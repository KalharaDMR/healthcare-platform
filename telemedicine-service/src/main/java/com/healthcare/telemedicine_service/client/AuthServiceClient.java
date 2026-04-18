package com.healthcare.telemedicine_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", path = "/internal")
public interface AuthServiceClient {

    @GetMapping("/users/by-username/{username}")
    AuthUserSummary getUserByUsername(@PathVariable("username") String username);
}
