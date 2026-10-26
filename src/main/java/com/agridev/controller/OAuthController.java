package com.agridev.controller;

import com.agridev.dto.LoginResponseDTO;
import com.agridev.dto.OAuthRequestDTO;
import com.agridev.service.AuthService;
import com.agridev.service.FacebookAuthService;
import com.agridev.service.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final FacebookAuthService facebookAuthService;

    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> googleLogin(@Valid @RequestBody OAuthRequestDTO request) {
        try {
            Map<String, Object> payload = googleAuthService.verifyToken(request.getToken());
            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            if (pictureUrl == null) {
                pictureUrl = (String) payload.get("avatar_url");
            }
            
            LoginResponseDTO response = authService.oauthLogin(email, name, pictureUrl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/facebook")
    public ResponseEntity<LoginResponseDTO> facebookLogin(@Valid @RequestBody OAuthRequestDTO request) {
        try {
            Map<String, Object> payload = facebookAuthService.verifyToken(request.getToken());
            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            
            String pictureUrl = null;
            if (payload.containsKey("picture")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> picture = (Map<String, Object>) payload.get("picture");
                if (picture != null && picture.containsKey("data")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) picture.get("data");
                    if (data != null) {
                        pictureUrl = (String) data.get("url");
                    }
                }
            }
            
            LoginResponseDTO response = authService.oauthLogin(email, name, pictureUrl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
