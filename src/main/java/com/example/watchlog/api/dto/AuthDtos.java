package com.example.watchlog.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "表示名は必須です")
            @Size(max = 100, message = "表示名は100文字以内で入力してください") String name,
            @NotBlank(message = "メールアドレスは必須です")
            @Email(message = "メールアドレスの形式が正しくありません") String email,
            @NotBlank(message = "パスワードは必須です")
            @Size(min = 8, max = 72, message = "パスワードは8〜72文字で入力してください") String password) {}

    public record LoginRequest(
            @NotBlank(message = "メールアドレスは必須です")
            @Email(message = "メールアドレスの形式が正しくありません") String email,
            @NotBlank(message = "パスワードは必須です") String password) {}

    public record UserResponse(Long id, String name, String email) {}
}
