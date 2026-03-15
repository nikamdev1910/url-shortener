package com.devn.urlshortener.service;

import com.devn.urlshortener.dto.ClickRequest;
import com.devn.urlshortener.dto.ClickStatsResponse;

public interface ClickService {

    void recordClick(ClickRequest clickRequest);

    ClickStatsResponse getClickStats(String statsToken);
}