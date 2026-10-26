package com.agridev.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Configuration class to enable global CORS settings
@Configuration
public class GlobalCorsConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    // Bean to configure CORS rules for the application
    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String cleanFrontendUrl = (frontendUrl != null && !frontendUrl.isBlank()) 
                        ? frontendUrl.replaceAll("/+$", "") 
                        : "http://localhost:5173";

                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                cleanFrontendUrl,
                                "http://localhost:*",
                                "http://127.0.0.1:*"
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

}
