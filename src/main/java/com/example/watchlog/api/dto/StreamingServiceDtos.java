package com.example.watchlog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.hibernate.validator.constraints.URL;

public final class StreamingServiceDtos {
    private StreamingServiceDtos() {}

    public record StreamingServiceRequest(
            @NotBlank(message = "サービス名は必須です")
            @Size(max = 100, message = "サービス名は100文字以内で入力してください") String name,
            @NotBlank(message = "サービスURLは必須です")
            @URL(message = "サービスURLの形式が正しくありません") String serviceUrl,
            @URL(message = "ロゴURLの形式が正しくありません") String logoUrl) {}

    public record StreamingServiceResponse(
            Long id,
            String name,
            String serviceUrl,
            String logoUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}
}
