package com.devn.urlshortener.service.impl;

import com.devn.urlshortener.exception.RateLimitExceededException;
import com.devn.urlshortener.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate:";

    @Value("${app.rate.limit}")
    private int rateLimit;

    @Value("${app.rate.limit.ttl}")
    private long rateLimitTtl;

    @Override
    public void validateRateLimit(String userIp) {
        String key = RATE_LIMIT_PREFIX + userIp;

        try {
            Long requestCount = redisTemplate.opsForValue().increment(key);
        
            if (requestCount == null) {
                log.error("Failed to increment rate limit counter for IP: {}", userIp);
                return;
            }
        
            if (requestCount == 1) {
                redisTemplate.expire(key, rateLimitTtl, TimeUnit.SECONDS);
                log.info("New rate limit window started for IP: {}", userIp);
            }
        
            log.info("IP: {} has made {}/{} requests this hour", userIp, requestCount, rateLimit);
        
            if (requestCount > rateLimit) {
                long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                log.warn("Rate limit exceeded for IP: {}. Resets in {} seconds", userIp, ttl);
                throw new RateLimitExceededException(
                    "Rate limit exceeded. You can create " + rateLimit +
                    " URLs per hour. Try again in " + ttl + " seconds."
                );
            }
        
        } catch (RateLimitExceededException e) {
            // Always rethrow rate limit exceptions — never swallow them
            throw e;
        } catch (Exception e) {
            // Only swallow Redis connection/technical failures
            log.error("Rate limit check failed for IP: {}", userIp, e);
        }
    }
}