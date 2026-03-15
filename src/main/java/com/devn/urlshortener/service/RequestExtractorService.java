package com.devn.urlshortener.service;

import com.devn.urlshortener.dto.ClickRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface RequestExtractorService {

    ClickRequest extract(String shortCode, HttpServletRequest request);
}