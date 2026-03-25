package com.healthcare.doctor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authentication-service", url = "${services.auth.url}")
public interface AuthServiceClient {

    @PutMapping("/internal/users/{id}/approve")
    void approveDoctor(
            @PathVariable("id") Long authUserId,
            @RequestHeader("X-INTERNAL-KEY") String internalKey
    );
}