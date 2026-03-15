package com.devn.urlshortener.service.impl;

import com.devn.urlshortener.dto.ClickRequest;
import com.devn.urlshortener.dto.ClickStatsResponse;
import com.devn.urlshortener.entity.Click;
import com.devn.urlshortener.entity.Url;
import com.devn.urlshortener.repository.ClickRepository;
import com.devn.urlshortener.repository.UrlRepository;
import com.devn.urlshortener.service.ClickService;
import com.devn.urlshortener.service.GeoLocationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickServiceImpl implements ClickService {

    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;
    private final GeoLocationService geoLocationService;

    @Value("${app.base.url}")
    private String baseUrl;

    @Async
    @Override
    public void recordClick(ClickRequest clickRequest) {

            Url url = urlRepository.findByShortCode(clickRequest.getShortCode())
                            .orElseThrow(() -> new RuntimeException(
                                            "Short URL not found: " + clickRequest.getShortCode()));

            String country = geoLocationService.getCountry(clickRequest.getIp());
            String city = geoLocationService.getCity(clickRequest.getIp());

            Click click = Click.builder()
                            .url(url)
                            .country(country)
                            .city(city)
                            .referrer(clickRequest.getReferrer())
                            .userAgent(clickRequest.getUserAgent())
                            .build();

            clickRepository.save(click);
            log.info("Click recorded for short code: {} from {}, {}",
                            clickRequest.getShortCode(), city, country);
    }

    @Override
    public ClickStatsResponse getClickStats(String statsToken) {

        Url url = urlRepository.findByStatsToken(statsToken)
                .orElseThrow(() -> new RuntimeException("Invalid stats token"));

        long totalClicks = clickRepository.countByUrlId(url.getId());

        // Clicks by country
        Map<String, Long> clicksByCountry = toMap(
                clickRepository.countClicksByCountry(url.getId())
        );

        // Clicks by referrer
        Map<String, Long> clicksByReferrer = toMap(
                clickRepository.countClicksByReferrer(url.getId())
        );

        // Clicks by date
        Map<String, Long> clicksByDate = toMap(
                clickRepository.countClicksByDate(url.getId())
        );

        return ClickStatsResponse.builder()
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .longUrl(url.getLongUrl())
                .totalClicks(totalClicks)
                .clicksByCountry(clicksByCountry)
                .clicksByReferrer(clicksByReferrer)
                .clicksByDate(clicksByDate)
                .build();
    }

    // Converts List<Object[]> from JPQL queries into a readable Map<String, Long>
    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] != null ? row[0].toString() : "Unknown";
            Long value = ((Number) row[1]).longValue();
            result.put(key, value);
        }
        return result;
    }
}