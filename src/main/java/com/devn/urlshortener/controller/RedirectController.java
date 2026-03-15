package com.devn.urlshortener.controller;

import com.devn.urlshortener.dto.ClickRequest;
import com.devn.urlshortener.service.ClickService;
import com.devn.urlshortener.service.RequestExtractorService;
import com.devn.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;
    private final ClickService clickService;
    private final RequestExtractorService requestExtractorService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        // Get original URL — throws if not found or expired
        String longUrl = urlService.getOriginalUrl(shortCode);

        // Extract data from request synchronously, record click asynchronously
        ClickRequest clickRequest = requestExtractorService.extract(shortCode, request);
        clickService.recordClick(clickRequest);

        // 302 redirect to original URL
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, longUrl);
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    private String extractIp(HttpServletRequest request) {
        String forwardedIp = request.getHeader("X-Forwarded-For");
        if (forwardedIp != null && !forwardedIp.isBlank()) {
            return forwardedIp.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}