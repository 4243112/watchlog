package com.example.watchlog.api.dto;

import com.example.watchlog.domain.WorkType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public final class TmdbDtos {
    private TmdbDtos() {}

    public record TmdbStatusResponse(boolean configured) {}

    public record TmdbSearchItem(
            Long tmdbId,
            String mediaType,
            WorkType type,
            String title,
            String overview,
            Integer releaseYear,
            String genre,
            String imageUrl,
            Double rating) {}

    public record TmdbSearchResponse(List<TmdbSearchItem> results) {}

    public record TmdbImportRequest(
            @NotNull(message = "TMDB作品IDは必須です") Long tmdbId,
            @NotNull(message = "メディア種別は必須です")
            @Pattern(regexp = "MOVIE|TV", message = "メディア種別はMOVIEまたはTVです") String mediaType) {}
}
