package com.healthcare.doctor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "AUTHENTICATION-SERVICE")
public interface AuthServiceClient {

    @GetMapping("/internal/users/{id}")
    Object getUser(@PathVariable("id") Long id);

    @PutMapping("/internal/users/{id}")
    void updateUser(@PathVariable("id") Long id, @RequestBody Object request);
}