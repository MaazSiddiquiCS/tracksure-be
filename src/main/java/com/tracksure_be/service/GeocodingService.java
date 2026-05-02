package com.tracksure_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for reverse geocoding using Nominatim (OpenStreetMap)
 * Converts coordinates (latitude, longitude) to human-readable addresses
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.nominatim.api-url:https://nominatim.openstreetmap.org}")
    private String nominatimApiUrl;

    @Value("${app.nominatim.timeout-ms:5000}")
    private long timeoutMs;

    /**
     * Reverse geocode coordinates to get address information
     */
    public Map<String, Object> reverseGeocode(Double latitude, Double longitude) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String url = String.format(
                    "%s/reverse?format=json&lat=%s&lon=%s&zoom=18&addressdetails=1&accept-language=en",
                    nominatimApiUrl, latitude, longitude
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TrackSure-BLE-Theft-Recovery/1.0 (Android Tracking App)");

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                // Extract display name (full formatted address)
                String displayName = jsonNode.has("display_name") 
                    ? jsonNode.get("display_name").asText("") 
                    : "";

                // Extract city/town/suburb from address object
                JsonNode address = jsonNode.has("address") 
                    ? jsonNode.get("address") 
                    : null;

                String city = extractCity(address);

                result.put("formattedAddress", displayName);
                result.put("city", city);
                result.put("latitude", latitude);
                result.put("longitude", longitude);
                result.put("success", true);
                result.put("rawAddress", address);

                log.debug("Geocoding successful for lat={}, lon={}. City: {}", latitude, longitude, city);
            } else {
                result.put("success", false);
                result.put("error", "Failed to reverse geocode: HTTP " + response.getStatusCode());
                log.warn("Geocoding failed with status: {}", response.getStatusCode());
            }
        } catch (RestClientException e) {
            result.put("success", false);
            result.put("error", "Network error during geocoding: " + e.getMessage());
            log.error("RestClient error during geocoding", e);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Error during geocoding: " + e.getMessage());
            log.error("Unexpected error during geocoding", e);
        }

        return result;
    }

    /**
     * Extract city from Nominatim address object
     * Tries city -> town -> suburb -> village in order of priority
     */
    private String extractCity(JsonNode address) {
        if (address == null) {
            return null;
        }

        // Try different address hierarchy levels
        String[] priorityFields = {"city", "town", "suburb", "village", "county"};
        
        for (String field : priorityFields) {
            if (address.has(field)) {
                return address.get(field).asText();
            }
        }

        return null;
    }

    /**
     * Validate coordinates are within valid ranges
     */
    public boolean isValidCoordinate(Double latitude, Double longitude) {
        return latitude != null && longitude != null &&
               latitude >= -90 && latitude <= 90 &&
               longitude >= -180 && longitude <= 180;
    }
}
