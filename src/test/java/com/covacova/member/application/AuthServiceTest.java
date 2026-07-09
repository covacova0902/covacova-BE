package com.covacova.member.application;

import com.covacova.global.security.auth.CustomUserDetails;
import com.covacova.global.security.jwt.JwtProperties;
import com.covacova.global.security.jwt.JwtTokenProvider;
import com.covacova.member.dto.response.LoginResponse;
import com.covacova.member.infrastructure.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    //JwtProperties가 record라서 목 생성이 안됨
    private final JwtProperties jwtProperties = new JwtProperties("test-secret-key", 3600000L, 1209600000L);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtTokenProvider, jwtProperties, refreshTokenRepository);
    }

    @Test
    void 정상_로그인시_액세스토큰과_리프레시토큰을_반환한다() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getMemberId()).willReturn(1L);

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);

        given(authenticationManager.authenticate(any())).willReturn(authentication);

        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access_token");

        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("refresh_token");

        LoginResponse response = authService.login("test@example.com", "password123");

        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.refreshToken()).isEqualTo("refresh_token");
    }

    @Test
    void 잘못된_자격증명이면_BadCredentialsException이_전파된다() {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException(""));

        assertThatThrownBy(() -> authService.login("test@example.com", "wrong-password"))
                .isInstanceOf(BadCredentialsException.class);
    }
}
