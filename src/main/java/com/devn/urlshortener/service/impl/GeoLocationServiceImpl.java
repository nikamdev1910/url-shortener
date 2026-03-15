package com.devn.urlshortener.service.impl;

import com.devn.urlshortener.service.GeoLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationServiceImpl implements GeoLocationService {

    private final RestTemplate restTemplate;

    @Value("${app.ipinfo.token}")
    private String ipInfoToken;

    private static final String IPINFO_URL = "https://ipinfo.io/{ip}?token={token}";
    private static final String LOCAL_IP_1 = "127.0.0.1";
    private static final String LOCAL_IP_2 = "0:0:0:0:0:0:0:1";

    @Override
    public String getCountry(String ipAddress) {
        Map<String, String> response = fetchIpInfo(ipAddress);
        return response != null ? response.get("country") : null;
    }

    @Override
    public String getCity(String ipAddress) {
        Map<String, String> response = fetchIpInfo(ipAddress);
        return response != null ? response.get("city") : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> fetchIpInfo(String ipAddress) {

        // Handle local development IPs
        if (LOCAL_IP_1.equals(ipAddress) || LOCAL_IP_2.equals(ipAddress)) {
            log.info("Local IP detected — skipping geo lookup");
            return Map.of("country", "Local", "city", "Local");
        }

        try {
            Map<String, String> response = restTemplate.getForObject(
                    IPINFO_URL,
                    Map.class,
                    ipAddress,
                    ipInfoToken
            );
            log.info("Geo lookup success for IP: {} → {}, {}",
                    ipAddress,
                    response != null ? response.get("country") : "null",
                    response != null ? response.get("city") : "null");
            return response;

        } catch (Exception e) {
            // Never let geo lookup failure break the click recording
            log.error("Geo lookup failed for IP: {}", ipAddress, e);
            return null;
        }
    }
}