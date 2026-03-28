package com.example.aisymptom_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "admin-service")
public interface ai_symptom_service_client {
    @GetMapping("/api/admin/specializations")
    List<String> getSpecializations();
}