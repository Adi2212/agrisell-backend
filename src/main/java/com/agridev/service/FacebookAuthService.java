package com.agridev.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FacebookAuthService {

    public Map<String, Object> verifyToken(String accessToken) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
            RestTemplate restTemplate = new RestTemplate();
            
            // Expected response: { "id": "...", "name": "...", "email": "...", "picture": { "data": { "url": "..." } } }
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("email")) {
                return response;
            } else {
                throw new IllegalArgumentException("Invalid Facebook token or email not provided.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error verifying Facebook token: " + e.getMessage());
        }
    }
}
