package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.AuthUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/internal/users/{id}")
    Object getUser(@PathVariable("id") Long id);

    @PutMapping("/internal/users/{id}")
    void updateUser(@PathVariable("id") Long id, @RequestBody Object request);

    @GetMapping("/internal/users/by-username/{username}")
    AuthUserDto getUserByUsername(@PathVariable("username") String username);
}