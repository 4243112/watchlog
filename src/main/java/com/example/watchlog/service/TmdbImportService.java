package com.example.watchlog.service;

import com.example.watchlog.api.dto.TmdbDtos.TmdbImportRequest;
import com.example.watchlog.api.dto.TmdbDtos.TmdbSearchItem;
import com.example.watchlog.api.dto.WorkDtos.WorkResponse;
import com.example.watchlog.domain.StreamingService;
import com.example.watchlog.domain.Work;
import com.example.watchlog.domain.WorkStreamingAvailability;
import com.example.watchlog.domain.WorkType;
import com.example.watchlog.integration.TmdbClient;
import com.example.watchlog.integration.TmdbClient.TmdbProvider;
import com.example.watchlog.integration.TmdbClient.TmdbTitleDetails;
import com.example.watchlog.repository.StreamingServiceRepository;
import com.example.watchlog.repository.WorkRepository;
import com.example.watchlog.repository.WorkStreamingAvailabilityRepository;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TmdbImportService {
    private static final String SOURCE = "TMDB";
    private final TmdbClient tmdbClient;
    private final WorkRepository workRepository;
    private final StreamingServiceRepository serviceRepository;
    private final WorkStreamingAvailabilityRepository availabilityRepository;
    private final WorkService workService;

    public TmdbImportService(TmdbClient tmdbClient, WorkRepository workRepository,
                             StreamingServiceRepository serviceRepository,
                             WorkStreamingAvailabilityRepository availabilityRepository,
                             WorkService workService) {
        this.tmdbClient = tmdbClient;
        this.workRepository = workRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
        this.workService = workService;
    }

    public boolean isConfigured() {
        return tmdbClient.isConfigured();
    }

    public List<TmdbSearchItem> search(String query, WorkType type) {
        return tmdbClient.search(query.trim(), type);
    }

    @Transactional
    public WorkResponse importTitle(TmdbImportRequest request) {
        Work existing = workRepository.findByExternalSourceAndExternalIdAndExternalMediaType(
                SOURCE, request.tmdbId(), request.mediaType()).orElse(null);
        if (existing != null) return workService.findOne(existing.getId());

        TmdbTitleDetails details = tmdbClient.details(request.tmdbId(), request.mediaType());
        Work work = new Work(
                limit(details.title(), 200),
                limit(blankToDefault(details.overview(), "あらすじは登録されていません。"), 4000),
                details.type(),
                details.releaseYear() == null ? Year.now().getValue() : details.releaseYear(),
                limit(blankToDefault(details.genre(), "その他"), 100),
                nullableLimit(details.director(), 150),
                details.imageUrl());
        work.setExternalMetadata(SOURCE, details.tmdbId(), details.mediaType());
        workRepository.save(work);
        importProviders(work, details.providers());
        return workService.findOne(work.getId());
    }

    private void importProviders(Work work, List<TmdbProvider> providers) {
        for (TmdbProvider provider : providers) {
            if (provider.name() == null || provider.name().isBlank()) continue;
            StreamingService service = serviceRepository.findByNameIgnoreCase(provider.name())
                    .orElseGet(() -> serviceRepository.save(new StreamingService(
                            limit(provider.name(), 100),
                            blankToDefault(provider.viewingUrl(), "https://www.themoviedb.org"),
                            provider.logoUrl())));
            if (!availabilityRepository.existsByWorkIdAndStreamingServiceId(work.getId(), service.getId())) {
                availabilityRepository.save(new WorkStreamingAvailability(
                        work, service, provider.viewingUrl(), provider.availabilityType()));
            }
        }
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String nullableLimit(String value, int max) {
        return value == null || value.isBlank() ? null : limit(value, max);
    }

    private String limit(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
