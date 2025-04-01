package com.example.new_portfolio_server.config.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisServiceImpl {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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
