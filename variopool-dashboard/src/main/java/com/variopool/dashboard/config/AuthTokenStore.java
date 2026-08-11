package com.variopool.dashboard.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthTokenStore {

    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000L;

    public String createToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, System.currentTimeMillis() + EXPIRE_MS);
        return token;
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Long expireAt = tokens.get(token);
        if (expireAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireAt) {
            tokens.remove(token);
            return false;
        }
        return true;
    }
}
