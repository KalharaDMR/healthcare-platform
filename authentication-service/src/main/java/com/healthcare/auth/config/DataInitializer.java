package com.healthcare.auth.config;

import com.healthcare.auth.entity.Role;
import com.healthcare.auth.entity.User;
import com.healthcare.auth.repository.RoleRepository;
import com.healthcare.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create roles if not exist
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
            Role doctorRole = roleRepository.findByName("DOCTOR").orElseGet(() -> roleRepository.save(new Role("DOCTOR")));
            Role patientRole = roleRepository.findByName("PATIENT").orElseGet(() -> roleRepository.save(new Role("PATIENT")));

            // Create admin user if not exist
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setName("ADMIN");
                admin.setEmail("admin@healthcare.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setStatus("ACTIVE");
                admin.setApproved(true);
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
                System.out.println("Admin user created: admin / admin123");
            }
        };
    }
}