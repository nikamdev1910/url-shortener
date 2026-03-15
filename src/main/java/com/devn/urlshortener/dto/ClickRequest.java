package com.devn.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClickRequest {

    private String shortCode;
    private String ip;
    private String referrer;
    private String userAgent;
}