package com.devn.urlshortener.service; 

import com.devn.urlshortener.dto.ClickRequest;
import com.devn.urlshortener.service.RequestExtractorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RequestExtractorServiceImpl implements RequestExtractorService {

    @Override
    public ClickRequest extract(String shortCode, HttpServletRequest request) {
        String ip = extractIp(request);
        String referrer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");

        log.info("Extracted request data for short code: {} from IP: {}", shortCode, ip);

        return ClickRequest.builder()
                .shortCode(shortCode)
                .ip(ip)
                .referrer(referrer)
                .userAgent(userAgent)
                .build();
    }

    private String extractIp(HttpServletRequest request) {
        String forwardedIp = request.getHeader("X-Forwarded-For");
        if (forwardedIp != null && !forwardedIp.isBlank()) {
            return forwardedIp.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}