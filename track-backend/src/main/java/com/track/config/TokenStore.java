package com.track.config;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final ConcurrentHashMap<String, Long> tokenMap = new ConcurrentHashMap<>();

    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenMap.put(token, userId);
        return token;
    }

    public Long getUserId(String token) {
        return tokenMap.get(token);
    }

    public void removeToken(String token) {
        tokenMap.remove(token);
    }
}
