package com.devn.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(
        regexp = "^(https?://).*",
        message = "URL must start with http:// or https://"
    )
    private String longUrl;

    // Optional — if null, URL never expires
    private Integer expiryDays;
}