package com.example.new_portfolio_server.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

// 리프레쉬 토큰 저장 14일 후 자동 만료
@Service
@RequiredArgsConstructor
public class RedisServiceImpl {
    private final RedisTemplate<String, String> redisTemplate;

    // 키-값 저장 및 만료 시간 설정
    public void setValues(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    // 키로 값 조회
    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 키 삭제
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
