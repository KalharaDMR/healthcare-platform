package com.healthcare.patient_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")  // Eureka service name
public interface AuthClient {

    @GetMapping("/api/users/{userId}")
    AuthUserResponse getUserById(@PathVariable("userId") String userId);
}