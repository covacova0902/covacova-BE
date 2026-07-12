package com.covacova.member.application;

import com.covacova.global.security.auth.CustomUserDetails;
import com.covacova.global.security.auth.CustomUserDetailsService;
import com.covacova.global.security.jwt.JwtProperties;
import com.covacova.global.security.jwt.JwtTokenProvider;
import com.covacova.member.exception.InvalidRefreshTokenException;
import com.covacova.member.infrastructure.RefreshTokenRepository;
import com.covacova.member.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long memberId = userDetails.getMemberId();

        String accessToken = jwtTokenProvider.createAccessToken(memberId);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        refreshTokenRepository.save(memberId, refreshToken,
                Duration.ofMillis(jwtProperties.refreshExpirationMillis()));

        return new LoginResponse(accessToken, refreshToken);
    }

    public String reissue(String refreshToken) {
        //만료 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        //레디스에 저장된 값과 쿠키로 들어온 값이 같아야 함
        String savedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!savedToken.equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        UserDetails userDetails = customUserDetailsService.loadUserByMemberId(memberId);
        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            throw new LockedException("");
        }

        //액세스 토큰만 새로 발급
        return jwtTokenProvider.createAccessToken(memberId);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return;
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        refreshTokenRepository.deleteByMemberId(memberId);
    }
}
