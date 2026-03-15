package com.devn.urlshortener.service.impl;

import com.devn.urlshortener.entity.Url;
import com.devn.urlshortener.service.UrlCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlCacheServiceImpl implements UrlCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_PREFIX = "url:";
    private static final long DEFAULT_TTL_SECONDS = 24 * 60 * 60; // 24 hours

    @Override
    public void cacheUrl(Url url) {
        try {
            String key = CACHE_PREFIX + url.getShortCode();
            long ttlSeconds = calculateTtl(url);

            if (ttlSeconds <= 0) {
                // URL already expired — don't cache it
                log.warn("Attempted to cache already expired URL: {}", url.getShortCode());
                return;
            }

            redisTemplate.opsForValue().set(key, url.getLongUrl(), ttlSeconds, TimeUnit.SECONDS);
            log.info("Cached URL: {} with TTL: {} seconds", url.getShortCode(), ttlSeconds);

        } catch (Exception e) {
            // Never let cache failure break the redirect flow
            log.error("Failed to cache URL: {}", url.getShortCode(), e);
        }
    }

    @Override
    public String getCachedUrl(String shortCode) {
        try {
            String key = CACHE_PREFIX + shortCode;
            String cachedUrl = redisTemplate.opsForValue().get(key);

            if (cachedUrl != null) {
                log.info("Cache HIT for short code: {}", shortCode);
            } else {
                log.info("Cache MISS for short code: {}", shortCode);
            }

            return cachedUrl;

        } catch (Exception e) {
            // Never let cache failure break the redirect flow
            log.error("Failed to get cached URL for: {}", shortCode, e);
            return null;
        }
    }

    @Override
    public void evictUrl(String shortCode) {
        try {
            String key = CACHE_PREFIX + shortCode;
            redisTemplate.delete(key);
            log.info("Evicted URL from cache: {}", shortCode);

        } catch (Exception e) {
            log.error("Failed to evict URL from cache: {}", shortCode, e);
        }
    }

    private long calculateTtl(Url url) {
        if (url.getExpiresAt() != null) {
            return ChronoUnit.SECONDS.between(LocalDateTime.now(), url.getExpiresAt());
        }
        return DEFAULT_TTL_SECONDS;
    }
}