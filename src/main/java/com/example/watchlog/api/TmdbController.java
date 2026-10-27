package com.example.watchlog.api;

import com.example.watchlog.api.dto.TmdbDtos.TmdbImportRequest;
import com.example.watchlog.api.dto.TmdbDtos.TmdbSearchResponse;
import com.example.watchlog.api.dto.TmdbDtos.TmdbStatusResponse;
import com.example.watchlog.api.dto.WorkDtos.WorkResponse;
import com.example.watchlog.domain.WorkType;
import com.example.watchlog.service.TmdbImportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/external/tmdb")
public class TmdbController {
    private final TmdbImportService importService;

    public TmdbController(TmdbImportService importService) {
        this.importService = importService;
    }

    @GetMapping("/status")
    public TmdbStatusResponse status() {
        return new TmdbStatusResponse(importService.isConfigured());
    }

    @GetMapping("/search")
    public TmdbSearchResponse search(
            @RequestParam @NotBlank(message = "検索語は必須です")
            @Size(max = 100, message = "検索語は100文字以内で入力してください") String query,
            @RequestParam(required = false) WorkType type) {
        return new TmdbSearchResponse(importService.search(query, type));
    }

    @PostMapping("/import")
    public ResponseEntity<WorkResponse> importTitle(@Valid @RequestBody TmdbImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.importTitle(request));
    }
}
