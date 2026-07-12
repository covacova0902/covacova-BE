package com.covacova.member.application;

import com.covacova.global.security.auth.CustomUserDetails;
import com.covacova.global.security.auth.CustomUserDetailsService;
import com.covacova.global.security.jwt.JwtProperties;
import com.covacova.global.security.jwt.JwtTokenProvider;
import com.covacova.member.dto.response.LoginResponse;
import com.covacova.member.exception.InvalidRefreshTokenException;
import com.covacova.member.infrastructure.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    //JwtProperties가 record라서 목 생성이 안됨
    private final JwtProperties jwtProperties = new JwtProperties("test-secret-key", 3600000L, 1209600000L);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtTokenProvider, jwtProperties, refreshTokenRepository, customUserDetailsService);
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

    @Test
    void 정상_재발급이면_새로운_액세스토큰을_반환한다() {
        given(jwtTokenProvider.validateToken("refresh_token")).willReturn(true);
        given(jwtTokenProvider.getMemberId("refresh_token")).willReturn(1L);
        given(refreshTokenRepository.findByMemberId(1L)).willReturn(Optional.of("refresh_token"));

        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.isEnabled()).willReturn(true);
        given(userDetails.isAccountNonLocked()).willReturn(true);
        given(customUserDetailsService.loadUserByMemberId(1L)).willReturn(userDetails);
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("new_access_token");

        String accessToken = authService.reissue("refresh_token");

        assertThat(accessToken).isEqualTo("new_access_token");
    }

    @Test
    void 유효하지_않은_토큰이면_재발급시_예외가_발생한다() {
        given(jwtTokenProvider.validateToken("invalid_token")).willReturn(false);

        assertThatThrownBy(() -> authService.reissue("invalid_token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void 토큰이_null이면_재발급시_예외가_발생한다() {
        assertThatThrownBy(() -> authService.reissue(null))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void 레디스에_저장된_값과_다르면_재발급시_예외가_발생한다() {
        given(jwtTokenProvider.validateToken("refresh_token")).willReturn(true);
        given(jwtTokenProvider.getMemberId("refresh_token")).willReturn(1L);
        given(refreshTokenRepository.findByMemberId(1L)).willReturn(Optional.of("다른_토큰"));

        assertThatThrownBy(() -> authService.reissue("refresh_token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void 레디스에_토큰이_없으면_재발급시_예외가_발생한다() {
        given(jwtTokenProvider.validateToken("refresh_token")).willReturn(true);
        given(jwtTokenProvider.getMemberId("refresh_token")).willReturn(1L);
        given(refreshTokenRepository.findByMemberId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue("refresh_token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void 탈퇴한_회원이면_재발급시_LockedException이_발생한다() {
        given(jwtTokenProvider.validateToken("refresh_token")).willReturn(true);
        given(jwtTokenProvider.getMemberId("refresh_token")).willReturn(1L);
        given(refreshTokenRepository.findByMemberId(1L)).willReturn(Optional.of("refresh_token"));

        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.isEnabled()).willReturn(false);
        given(customUserDetailsService.loadUserByMemberId(1L)).willReturn(userDetails);

        assertThatThrownBy(() -> authService.reissue("refresh_token"))
                .isInstanceOf(LockedException.class);
    }

    @Test
    void 정상_로그아웃이면_레디스에서_리프레시토큰을_삭제한다() {
        given(jwtTokenProvider.validateToken("refresh_token")).willReturn(true);
        given(jwtTokenProvider.getMemberId("refresh_token")).willReturn(1L);

        authService.logout("refresh_token");

        verify(refreshTokenRepository).deleteByMemberId(1L);
    }

    @Test
    void 유효하지_않은_토큰으로_로그아웃하면_아무일도_하지_않는다() {
        given(jwtTokenProvider.validateToken("invalid_token")).willReturn(false);

        authService.logout("invalid_token");

        verify(refreshTokenRepository, never()).deleteByMemberId(any());
    }
}
