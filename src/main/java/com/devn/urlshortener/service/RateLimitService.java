package com.devn.urlshortener.service;

public interface RateLimitService {
    void validateRateLimit(String userIp);
}
