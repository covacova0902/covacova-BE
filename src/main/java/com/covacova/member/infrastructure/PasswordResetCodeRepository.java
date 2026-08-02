package com.covacova.member.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class PasswordResetCodeRepository {

    private static final String KEY_PREFIX = "password-reset-code:";

    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public void save(String email, String code) {
        PasswordResetCode value = new PasswordResetCode(code, 0);
        redisTemplate.opsForValue().set(KEY_PREFIX + email, serialize(value), TTL);
    }

    private String serialize(PasswordResetCode value) {
        return objectMapper.writeValueAsString(value);
    }
}
