package com.example.watchlog.api.dto;

import com.example.watchlog.domain.ViewingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ViewingHistoryDtos {
    private ViewingHistoryDtos() {}

    public record CreateViewingHistoryRequest(
            @NotNull(message = "作品IDは必須です") Long workId,
            @NotNull(message = "視聴状態は必須です") ViewingStatus status,
            LocalDate watchedAt,
            @Min(value = 1, message = "評価は1〜5で入力してください")
            @Max(value = 5, message = "評価は1〜5で入力してください") Integer rating,
            @Size(max = 2000, message = "感想は2000文字以内で入力してください") String comment) {}

    public record UpdateViewingHistoryRequest(
            @NotNull(message = "視聴状態は必須です") ViewingStatus status,
            LocalDate watchedAt,
            @Min(value = 1, message = "評価は1〜5で入力してください")
            @Max(value = 5, message = "評価は1〜5で入力してください") Integer rating,
            @Size(max = 2000, message = "感想は2000文字以内で入力してください") String comment) {}

    public record WorkSummary(Long id, String title, String imageUrl, String genre, Integer releaseYear) {}

    public record ViewingHistoryResponse(
            Long id,
            Long userId,
            WorkSummary work,
            ViewingStatus status,
            LocalDate watchedAt,
            Integer rating,
            String comment,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}
}
