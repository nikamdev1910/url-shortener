package com.devn.urlshortener.service.impl;

import com.devn.urlshortener.dto.CreateUrlRequest;
import com.devn.urlshortener.dto.CreateUrlResponse;
import com.devn.urlshortener.entity.Url;
import com.devn.urlshortener.repository.UrlRepository;
import com.devn.urlshortener.service.RateLimitService;
import com.devn.urlshortener.service.UrlCacheService;
import com.devn.urlshortener.service.UrlService;
import com.devn.urlshortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlCacheService urlCacheService;
    private final RateLimitService rateLimitService;

    @Value("${app.base.url}")
    private String baseUrl;

    @Override
    public CreateUrlResponse createShortUrl(CreateUrlRequest request, String userIp) {

        // Step 1 — check rate limit before doing anything
        rateLimitService.validateRateLimit(userIp);

        // Step 2 — save with placeholder to get DB id
        Url url = Url.builder()
                .shortCode("tmp")
                .statsToken(shortCodeGenerator.generateStatsToken())
                .longUrl(request.getLongUrl())
                .userIp(userIp)
                .expiresAt(request.getExpiryDays() != null
                        ? LocalDateTime.now().plusDays(request.getExpiryDays())
                        : null)
                .build();

        Url saved = urlRepository.save(url);

        // Step 3 — generate short code from ID and update
        String shortCode = shortCodeGenerator.generateShortCode(saved.getId());
        saved.setShortCode(shortCode);
        urlRepository.save(saved);

        // Step 4 — cache the newly created URL immediately
        urlCacheService.cacheUrl(saved);

        return CreateUrlResponse.builder()
                .shortUrl(baseUrl + "/" + shortCode)
                .statsUrl(baseUrl + "/api/stats/" + shortCode + "?token=" + saved.getStatsToken())
                .longUrl(saved.getLongUrl())
                .expiresAt(saved.getExpiresAt() != null
                        ? saved.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "Never")
                .build();
    }

    @Override
    public String getOriginalUrl(String shortCode) {

        // Step 1 — check Redis cache first
        String cachedUrl = urlCacheService.getCachedUrl(shortCode);
        if (cachedUrl != null) {
            return cachedUrl;
        }

        // Step 2 — cache miss, hit Postgres
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));

        // Step 3 — check expiry
        if (isExpired(url)) {
            // Evict from cache if somehow it's there
            urlCacheService.evictUrl(shortCode);
            throw new RuntimeException("Short URL has expired: " + shortCode);
        }

        // Step 4 — store in cache for next time
        urlCacheService.cacheUrl(url);

        return url.getLongUrl();
    }

    @Override
    public boolean isExpired(Url url) {
        return url.getExpiresAt() != null && LocalDateTime.now().isAfter(url.getExpiresAt());
    }
}