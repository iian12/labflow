package com.labflow.auth.presentation;

import com.google.common.net.HttpHeaders;
import com.labflow.auth.application.AuthService;
import com.labflow.auth.application.LoginResult;
import com.labflow.auth.infrastructure.cookie.AuthCookieProvider;
import com.labflow.user.domain.UserId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieProvider authCookieProvider;

    public AuthController(AuthService authService, AuthCookieProvider authCookieProvider) {
        this.authService = authService;
        this.authCookieProvider = authCookieProvider;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("signUp request: {}", request);
        UserId userId = authService.signUp(request.toCommand());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResult result = authService.login(request.toCommand());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieProvider.createAccessTokenCookie(result.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieProvider.createRefreshTokenCookie(result.refreshToken(), result.refreshTokenExpiresAt()).toString());

        return ResponseEntity.noContent().build();
    }
}
