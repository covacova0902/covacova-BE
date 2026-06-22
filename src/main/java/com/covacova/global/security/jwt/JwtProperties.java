package com.covacova.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessExpirationMillis,
        long refreshExpirationMillis
) {
}
