package com.healthcare.admin.config;

import com.healthcare.admin.entity.Specialization;
import com.healthcare.admin.repository.SpecializationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initSpecializations(SpecializationRepository repo) {
        return args -> {
            List<String> defaultSpecializations = List.of(
                    "Cardiology", "Dermatology", "Pediatrics", "Neurology", "Orthopedics"
            );
            for (String name : defaultSpecializations) {
                if (repo.findByName(name).isEmpty()) {
                    repo.save(new Specialization(name));
                }
            }
        };
    }
}