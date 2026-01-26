package com.agridev.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.agridev.model.Role;
import com.agridev.model.User;
import com.agridev.repository.UserRepository;

import java.util.Random;

// Utility configuration class for application level beans
@Configuration
public class AppUtil {




    // Bean for password encoding
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean for object mapping between DTO and Entity
    @Bean
    public ModelMapper modelMapper() {

        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setSkipNullEnabled(true);

        return mapper;
    }

    // Method to initialize default admin user at application startup
    @Bean
    CommandLineRunner initAdmin(UserRepository repo, BCryptPasswordEncoder encoder) {

        return args -> {

            if (repo.findByEmail("admin@agrisell.com").isEmpty()) {

                User admin = new User();

                admin.setName("System Admin");
                admin.setEmail("admin@agrisell.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);

                repo.save(admin);

                System.out.println("Admin user created with default credentials");
            }
        };
    }

}
