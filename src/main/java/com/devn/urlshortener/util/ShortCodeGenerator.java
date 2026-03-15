package com.devn.urlshortener.util;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sqids.Sqids;

@Component
public class ShortCodeGenerator {
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int STATS_TOKEN_LENGTH = 12;
    private final SecureRandom random = new SecureRandom();
    private final Sqids sqids;

    public ShortCodeGenerator(@Value("${app.sqids.alphabet}") String alphabet) {
        this.sqids = Sqids.builder()
                .alphabet(alphabet)
                .minLength(6)
                .build();
    }

    // Encode DB id to Base62 — sequential, zero collision
    public String generateShortCode(Long id) {
        return sqids.encode(List.of(id));
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
