package com.covacova.member.presentation;

import com.covacova.global.response.ApiResponse;
import com.covacova.global.security.jwt.JwtProperties;
import com.covacova.member.application.AuthService;
import com.covacova.member.dto.request.LoginRequest;
import com.covacova.member.dto.response.AccessTokenResponse;
import com.covacova.member.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "인증", description = "로그인/로그아웃 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private static final String COOKIE_PATH = "/api/auth";

    private final AuthService authService;

    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request.email(), request.password());

        ResponseCookie cookie = buildRefreshCookie(loginResponse.refreshToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(new AccessTokenResponse(loginResponse.accessToken())));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> reissue(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
                String accessToken = authService.reissue(refreshToken);

                return ResponseEntity.ok(ApiResponse.success(new AccessTokenResponse(accessToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        authService.logout(refreshToken);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, buildExpiredRefreshCookie().toString())
                .body(ApiResponse.success(null));
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                //자바스크립트가 쿠키를 못읽게
                .httpOnly(true)
                //배포시에는 true로 변경 필요!!
                .secure(false)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(Duration.ofMillis(jwtProperties.refreshExpirationMillis()))
                .build();
    }

    private ResponseCookie buildExpiredRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
