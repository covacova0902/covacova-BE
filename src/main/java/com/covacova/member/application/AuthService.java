package com.covacova.member.application;

import com.covacova.global.security.auth.CustomUserDetails;
import com.covacova.global.security.jwt.JwtProperties;
import com.covacova.global.security.jwt.JwtTokenProvider;
import com.covacova.member.infrastructure.RefreshTokenRepository;
import com.covacova.member.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

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
}
