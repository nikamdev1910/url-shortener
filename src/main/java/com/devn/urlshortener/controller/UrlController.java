package com.devn.urlshortener.controller;

import com.devn.urlshortener.dto.ClickStatsResponse;
import com.devn.urlshortener.dto.CreateUrlRequest;
import com.devn.urlshortener.dto.CreateUrlResponse;
import com.devn.urlshortener.entity.Url;
import com.devn.urlshortener.repository.UrlRepository;
import com.devn.urlshortener.service.ClickService;
import com.devn.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final ClickService clickService;
    private final UrlRepository urlRepository;

    @PostMapping("/shorten")
    public ResponseEntity<CreateUrlResponse> shortenUrl(
            @Valid @RequestBody CreateUrlRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        // X-Forwarded-For is the real IP when behind a proxy (Render uses a proxy)
        String userIp = forwardedIp != null ? forwardedIp : httpRequest.getRemoteAddr();

        CreateUrlResponse response = urlService.createShortUrl(request, userIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<ClickStatsResponse> getStats(
            @PathVariable String shortCode,
            @RequestParam(value = "token", required = false) String token) {

        // Validate token is present
        if (token == null || token.isBlank()) {
            throw new SecurityException("Stats token is missing");
        }

        // Validate token belongs to this short code
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));

        if (!url.getStatsToken().equals(token)) {
            throw new SecurityException("Invalid stats token");
        }

        ClickStatsResponse response = clickService.getClickStats(token);
        return ResponseEntity.ok(response);
    }
}