package com.devn.urlshortener.service;

import com.devn.urlshortener.dto.ClickStatsResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface ClickService {

    void recordClick(String shortCode, HttpServletRequest request);

    ClickStatsResponse getClickStats(String statsToken);
}