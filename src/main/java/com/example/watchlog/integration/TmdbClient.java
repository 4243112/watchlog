package com.example.watchlog.integration;

import com.example.watchlog.api.ApiException;
import com.example.watchlog.api.dto.TmdbDtos.TmdbSearchItem;
import com.example.watchlog.domain.AvailabilityType;
import com.example.watchlog.domain.WorkType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class TmdbClient {
    private static final Map<Integer, String> GENRES = Map.ofEntries(
            Map.entry(16, "アニメ"), Map.entry(28, "アクション"), Map.entry(12, "アドベンチャー"),
            Map.entry(35, "コメディ"), Map.entry(80, "犯罪"), Map.entry(99, "ドキュメンタリー"),
            Map.entry(18, "ドラマ"), Map.entry(10751, "ファミリー"), Map.entry(14, "ファンタジー"),
            Map.entry(36, "歴史"), Map.entry(27, "ホラー"), Map.entry(10402, "音楽"),
            Map.entry(9648, "ミステリー"), Map.entry(10749, "ロマンス"), Map.entry(878, "SF"),
            Map.entry(10770, "テレビ映画"), Map.entry(53, "スリラー"), Map.entry(10752, "戦争"),
            Map.entry(37, "西部劇"), Map.entry(10759, "アクション・アドベンチャー"),
            Map.entry(10762, "キッズ"), Map.entry(10763, "ニュース"), Map.entry(10764, "リアリティ"),
            Map.entry(10765, "SF・ファンタジー"), Map.entry(10766, "ソープ"),
            Map.entry(10767, "トーク"), Map.entry(10768, "戦争・政治"));

    private final String token;
    private final String imageBaseUrl;
    private final String language;
    private final String region;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TmdbClient(
            @Value("${tmdb.api.base-url}") String baseUrl,
            @Value("${tmdb.api.image-base-url}") String imageBaseUrl,
            @Value("${tmdb.api.read-access-token:}") String token,
            @Value("${tmdb.api.language:ja-JP}") String language,
            @Value("${tmdb.api.region:JP}") String region,
            ObjectMapper objectMapper) {
        this.token = token == null ? "" : token.trim();
        this.imageBaseUrl = imageBaseUrl;
        this.language = language;
        this.region = region;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean isConfigured() {
        return !token.isBlank();
    }

    public List<TmdbSearchItem> search(String query, WorkType filter) {
        requireConfigured();
        List<TmdbSearchItem> results = new ArrayList<>();
        if (filter == null || filter == WorkType.MOVIE || filter == WorkType.ANIME) {
            results.addAll(parseSearch(get("/search/movie", Map.of(
                    "query", query, "language", language, "region", region,
                    "include_adult", "false", "page", "1")), "MOVIE"));
        }
        if (filter == null || filter == WorkType.DRAMA || filter == WorkType.ANIME) {
            results.addAll(parseSearch(get("/search/tv", Map.of(
                    "query", query, "language", language, "include_adult", "false", "page", "1")), "TV"));
        }
        return results.stream()
                .filter(item -> filter == null || item.type() == filter)
                .sorted(Comparator.comparing(TmdbSearchItem::rating, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .toList();
    }

    public TmdbTitleDetails details(Long tmdbId, String mediaType) {
        requireConfigured();
        String namespace = "MOVIE".equals(mediaType) ? "movie" : "tv";
        JsonNode details = read(get("/" + namespace + "/" + tmdbId, Map.of(
                "language", language, "append_to_response", "credits")));
        JsonNode providers = read(get("/" + namespace + "/" + tmdbId + "/watch/providers", Map.of()));
        return parseDetails(tmdbId, mediaType, details, providers);
    }

    List<TmdbSearchItem> parseSearch(String json, String mediaType) {
        List<TmdbSearchItem> results = new ArrayList<>();
        for (JsonNode item : read(json).path("results")) {
            boolean anime = containsGenre(item.path("genre_ids"), 16);
            WorkType type = anime ? WorkType.ANIME : ("MOVIE".equals(mediaType) ? WorkType.MOVIE : WorkType.DRAMA);
            String title = text(item, "MOVIE".equals(mediaType) ? "title" : "name");
            if (title.isBlank()) continue;
            results.add(new TmdbSearchItem(
                    item.path("id").asLong(), mediaType, type, title, text(item, "overview"),
                    year(text(item, "MOVIE".equals(mediaType) ? "release_date" : "first_air_date")),
                    genreNames(item.path("genre_ids")), imageUrl(text(item, "poster_path"), "w500"),
                    item.path("vote_average").asDouble(0)));
        }
        return results;
    }

    private TmdbTitleDetails parseDetails(Long tmdbId, String mediaType, JsonNode item, JsonNode providerRoot) {
        boolean movie = "MOVIE".equals(mediaType);
        boolean anime = false;
        List<String> genreNames = new ArrayList<>();
        for (JsonNode genre : item.path("genres")) {
            int id = genre.path("id").asInt();
            anime |= id == 16;
            String name = text(genre, "name");
            if (!name.isBlank()) genreNames.add(name);
        }
        String director = movie ? movieDirectors(item.path("credits").path("crew")) : creators(item.path("created_by"));
        return new TmdbTitleDetails(
                tmdbId, mediaType, anime ? WorkType.ANIME : (movie ? WorkType.MOVIE : WorkType.DRAMA),
                text(item, movie ? "title" : "name"), text(item, "overview"),
                year(text(item, movie ? "release_date" : "first_air_date")),
                genreNames.isEmpty() ? "その他" : String.join("・", genreNames), director,
                imageUrl(text(item, "poster_path"), "w500"), parseProviders(providerRoot));
    }

    private List<TmdbProvider> parseProviders(JsonNode root) {
        JsonNode regionResult = root.path("results").path(region);
        String link = text(regionResult, "link");
        LinkedHashMap<Integer, TmdbProvider> providers = new LinkedHashMap<>();
        addProviders(providers, regionResult.path("flatrate"), AvailabilityType.SUBSCRIPTION, link);
        addProviders(providers, regionResult.path("free"), AvailabilityType.SUBSCRIPTION, link);
        addProviders(providers, regionResult.path("ads"), AvailabilityType.SUBSCRIPTION, link);
        addProviders(providers, regionResult.path("rent"), AvailabilityType.RENTAL, link);
        addProviders(providers, regionResult.path("buy"), AvailabilityType.PURCHASE, link);
        return new ArrayList<>(providers.values());
    }

    private void addProviders(Map<Integer, TmdbProvider> target, JsonNode items,
                              AvailabilityType type, String link) {
        for (JsonNode provider : items) {
            int id = provider.path("provider_id").asInt();
            target.putIfAbsent(id, new TmdbProvider(id, text(provider, "provider_name"),
                    imageUrl(text(provider, "logo_path"), "w185"), link, type));
        }
    }

    private String get(String path, Map<String, String> params) {
        try {
            return restClient.get()
                    .uri(builder -> {
                        builder.path(path);
                        params.forEach(builder::queryParam);
                        return builder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "TMDB_REQUEST_FAILED", "TMDBから作品情報を取得できませんでした");
        }
    }

    private JsonNode read(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "TMDB_RESPONSE_INVALID", "TMDBの応答を読み取れませんでした");
        }
    }

    private boolean containsGenre(JsonNode genres, int expected) {
        for (JsonNode genre : genres) if (genre.asInt() == expected) return true;
        return false;
    }

    private String genreNames(JsonNode genres) {
        List<String> names = new ArrayList<>();
        for (JsonNode genre : genres) {
            String name = GENRES.get(genre.asInt());
            if (name != null) names.add(name);
        }
        return names.isEmpty() ? "その他" : String.join("・", names);
    }

    private String movieDirectors(JsonNode crew) {
        List<String> names = new ArrayList<>();
        for (JsonNode member : crew) {
            if ("Director".equals(text(member, "job"))) names.add(text(member, "name"));
            if (names.size() == 2) break;
        }
        return String.join("・", names);
    }

    private String creators(JsonNode creators) {
        List<String> names = new ArrayList<>();
        for (JsonNode creator : creators) {
            String name = text(creator, "name");
            if (!name.isBlank()) names.add(name);
            if (names.size() == 2) break;
        }
        return String.join("・", names);
    }

    private String text(JsonNode node, String field) {
        return node.path(field).isMissingNode() || node.path(field).isNull() ? "" : node.path(field).asText("");
    }

    private Integer year(String date) {
        if (date == null || date.length() < 4) return null;
        try { return Integer.parseInt(date.substring(0, 4)); }
        catch (NumberFormatException ex) { return null; }
    }

    private String imageUrl(String path, String size) {
        return path == null || path.isBlank() ? null : imageBaseUrl + "/" + size + path;
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "TMDB_NOT_CONFIGURED",
                    "TMDB連携には環境変数TMDB_READ_ACCESS_TOKENの設定が必要です");
        }
    }

    public record TmdbTitleDetails(
            Long tmdbId, String mediaType, WorkType type, String title, String overview,
            Integer releaseYear, String genre, String director, String imageUrl,
            List<TmdbProvider> providers) {}

    public record TmdbProvider(
            Integer providerId, String name, String logoUrl, String viewingUrl,
            AvailabilityType availabilityType) {}
}
