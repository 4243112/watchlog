package com.example.watchlog.service;

import com.example.watchlog.api.ApiException;
import com.example.watchlog.api.dto.AuthDtos.LoginRequest;
import com.example.watchlog.api.dto.AuthDtos.RegisterRequest;
import com.example.watchlog.api.dto.AuthDtos.UserResponse;
import com.example.watchlog.domain.AppUser;
import com.example.watchlog.repository.AppUserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "このメールアドレスはすでに登録されています");
        }
        AppUser user = userRepository.save(new AppUser(
                request.name().trim(), email, passwordEncoder.encode(request.password())));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw invalidCredentials();
        }
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "ログインが必要です"));
        return toResponse(user);
    }

    public AppUser requireEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "指定されたユーザーが見つかりません"));
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "メールアドレスまたはパスワードが正しくありません");
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
