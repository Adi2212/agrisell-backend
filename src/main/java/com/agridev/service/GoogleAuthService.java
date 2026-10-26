package com.agridev.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class GoogleAuthService {

    public Map<String, Object> verifyToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Try standard Authorization: Bearer header
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("email")) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Bearer auth check failed for Google token, trying query param fallback: {}", e.getMessage());
        }

        // 2. Fallback to query param
        try {
            String url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("email")) {
                return response;
            } else {
                throw new IllegalArgumentException("Invalid Google token or email not provided.");
            }
        } catch (Exception e) {
            log.error("Error verifying Google token: {}", e.getMessage());
            throw new IllegalArgumentException("Error verifying Google token: " + e.getMessage());
        }
    }
}
