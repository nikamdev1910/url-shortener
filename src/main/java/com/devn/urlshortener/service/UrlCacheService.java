package com.devn.urlshortener.service;

import com.devn.urlshortener.entity.Url;

public interface UrlCacheService {
    void cacheUrl(Url url);

    String getCachedUrl(String shortCode);

    void evictUrl(String shortCode);
}
