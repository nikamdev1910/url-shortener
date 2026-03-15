package com.devn.urlshortener.service;

public interface GeoLocationService {
    
    String getCountry(String ipAddress);

    String getCity(String ipAddress);
}
