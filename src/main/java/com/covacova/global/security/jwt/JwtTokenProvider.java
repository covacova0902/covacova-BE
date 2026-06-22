package com.covacova.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, jwtProperties.accessExpirationMillis());
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, jwtProperties.refreshExpirationMillis());
    }

    private String createToken(Long memberId, long expirationMillis) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("형식이 잘못된 토큰입니다: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("서명이 일치하지 않는 토큰입니다(위변조 가능성): {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 형식의 토큰입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("토큰 값이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    public Long getMemberId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
