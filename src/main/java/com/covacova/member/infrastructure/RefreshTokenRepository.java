package com.covacova.member.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String token, Duration ttl) {
        //key-value 방식
        redisTemplate.opsForValue().set(KEY_PREFIX + memberId, token, ttl);
    }

    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + memberId));
    }

    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(KEY_PREFIX + memberId);
    }
}
