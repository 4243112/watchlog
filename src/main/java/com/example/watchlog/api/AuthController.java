package com.example.watchlog.api;

import com.example.watchlog.api.dto.AuthDtos.LoginRequest;
import com.example.watchlog.api.dto.AuthDtos.RegisterRequest;
import com.example.watchlog.api.dto.AuthDtos.UserResponse;
import com.example.watchlog.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public static final String SESSION_USER_ID = "WATCHLOG_USER_ID";
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        UserResponse user = authService.register(request);
        session.setAttribute(SESSION_USER_ID, user.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        UserResponse user = authService.login(request);
        session.setAttribute(SESSION_USER_ID, user.id());
        return user;
    }

    @GetMapping("/me")
    public UserResponse me(HttpSession session) {
        return authService.getUser(requireLogin(session));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    public static Long requireLogin(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (!(userId instanceof Long id)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "ログインが必要です");
        }
        return id;
    }

    public static void requireOwner(HttpSession session, Long targetUserId) {
        Long signedInUserId = requireLogin(session);
        if (!signedInUserId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "VIEWING_HISTORY_FORBIDDEN", "他のユーザーの視聴履歴は操作できません");
        }
    }
}
