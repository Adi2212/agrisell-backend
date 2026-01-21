package com.agridev.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller to generate ImageKit authentication parameters
@RestController
@RequestMapping("/imagekit")
public class ImageKitAuthController {

    @Value("${imagekit.privateKey}")
    private String privateKey;

    // API to generate ImageKit token, expiry and signature
    @GetMapping
    public ResponseEntity<Map<String, String>> getImageKitAuth() {

        try {
            String token = UUID.randomUUID().toString();
            long expire = (System.currentTimeMillis() / 1000L) + 900;

            String data = token + expire;

            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey =
                    new SecretKeySpec(privateKey.getBytes(), "HmacSHA1");

            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());

            StringBuilder signatureBuilder = new StringBuilder();
            for (byte b : rawHmac) {
                signatureBuilder.append(String.format("%02x", b));
            }

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("expire", String.valueOf(expire));
            response.put("signature", signatureBuilder.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Error generating ImageKit auth parameters", e);
        }
    }

}
