package com.track.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Map<String, Long> tokenUserMap = new ConcurrentHashMap<>();

    public void put(String token, Long userId) {
        tokenUserMap.put(token, userId);
    }

    public Long getUserId(String token) {
        return tokenUserMap.get(token);
    }

    public void remove(String token) {
        tokenUserMap.remove(token);
    }
}
