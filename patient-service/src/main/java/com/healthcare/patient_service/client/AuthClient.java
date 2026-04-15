package com.healthcare.patient_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/internal/users/{id}")
    Object getUserById(@PathVariable("id") Long id,
                       @RequestHeader("X-INTERNAL-KEY") String apiKey);

    @GetMapping("/internal/users")
    List<Object> getAllUsers(@RequestHeader("X-INTERNAL-KEY") String apiKey);
}