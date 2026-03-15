package com.devn.urlshortener.util;

import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int STATS_TOKEN_LENGTH = 12;
    private final java.security.SecureRandom random = new java.security.SecureRandom();

    // Encode DB id to Base62 — sequential, zero collision
    public String generateShortCode(Long id) {
        if (id == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62.charAt((int)(id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    // Stats token stays random
    public String generateStatsToken() {
        StringBuilder sb = new StringBuilder(STATS_TOKEN_LENGTH);
        for (int i = 0; i < STATS_TOKEN_LENGTH; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
