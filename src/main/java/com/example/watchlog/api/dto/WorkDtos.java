package com.example.watchlog.api.dto;

import com.example.watchlog.domain.AvailabilityType;
import com.example.watchlog.domain.WorkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public final class WorkDtos {
    private WorkDtos() {}

    public record WorkRequest(
            @NotBlank(message = "作品名は必須です")
            @Size(max = 200, message = "作品名は200文字以内で入力してください") String title,
            @NotBlank(message = "あらすじは必須です")
            @Size(max = 4000, message = "あらすじは4000文字以内で入力してください") String description,
            @NotNull(message = "作品種別は必須です") WorkType type,
            @NotNull(message = "公開年は必須です")
            @Min(value = 1000, message = "公開年は4桁で入力してください")
            @Max(value = 9999, message = "公開年は4桁で入力してください") Integer releaseYear,
            @NotBlank(message = "ジャンルは必須です")
            @Size(max = 100, message = "ジャンルは100文字以内で入力してください") String genre,
            @Size(max = 150, message = "監督名は150文字以内で入力してください") String director,
            @URL(message = "ポスター画像URLの形式が正しくありません") String imageUrl) {}

    public record StreamingOptionResponse(
            Long id,
            Long streamingServiceId,
            String name,
            String serviceUrl,
            String logoUrl,
            String viewingUrl,
            AvailabilityType availabilityType) {}

    public record WorkResponse(
            Long id,
            String title,
            String description,
            WorkType type,
            Integer releaseYear,
            String genre,
            String director,
            String imageUrl,
            String externalSource,
            Long externalId,
            String externalMediaType,
            LocalDateTime externalUpdatedAt,
            List<StreamingOptionResponse> streamingServices,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    public record AvailabilityRequest(
            @NotNull(message = "配信サービスIDは必須です") Long streamingServiceId,
            @URL(message = "視聴URLの形式が正しくありません") String viewingUrl,
            @NotNull(message = "配信種別は必須です") AvailabilityType availabilityType) {}

    public record AvailabilityUpdateRequest(
            @URL(message = "視聴URLの形式が正しくありません") String viewingUrl,
            @NotNull(message = "配信種別は必須です") AvailabilityType availabilityType) {}
}
