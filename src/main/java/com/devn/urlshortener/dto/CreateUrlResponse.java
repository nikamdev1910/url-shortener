package com.devn.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUrlResponse {

    private String shortUrl;
    private String statsUrl;
    private String longUrl;
    private String expiresAt;
}