package com.devn.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ClickStatsResponse {

    private String shortUrl;
    private String longUrl;
    private long totalClicks;
    private Map<String, Long> clicksByCountry;
    private Map<String, Long> clicksByReferrer;
    private Map<String, Long> clicksByDate;
}