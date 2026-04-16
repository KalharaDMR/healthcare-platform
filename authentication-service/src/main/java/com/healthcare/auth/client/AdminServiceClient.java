package com.healthcare.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "admin-service")
public interface AdminServiceClient {

    @GetMapping("/api/admin/specializations")
    List<String> getSpecializations();
}