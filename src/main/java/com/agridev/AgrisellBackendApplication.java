package com.agridev;

import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AgrisellBackendApplication {

	@PostConstruct
	public void init() {
		// Set default application timezone to IST (Asia/Kolkata)
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

	public static void main(String[] args) {
		// Guarantee JVM default timezone is Asia/Kolkata before any beans initialize
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(AgrisellBackendApplication.class, args);
	}

}

