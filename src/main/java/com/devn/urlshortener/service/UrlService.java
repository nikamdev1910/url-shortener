package com.devn.urlshortener.service;

import com.devn.urlshortener.dto.CreateUrlRequest;
import com.devn.urlshortener.dto.CreateUrlResponse;
// import com.devn.urlshortener.dto.ClickStatsResponse;
import com.devn.urlshortener.entity.Url;

public interface UrlService {

    CreateUrlResponse createShortUrl(CreateUrlRequest request, String userIp);

    String getOriginalUrl(String shortCode);

    boolean isExpired(Url url);
}