package com.healthcare.auth.client;

import com.healthcare.auth.dto.DoctorMetaSyncRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "doctor-service")
public interface DoctorServiceClient {

    @PostMapping("/internal/doctor/meta/sync")
    void syncDoctorMeta(@RequestBody DoctorMetaSyncRequest request,
                        @RequestHeader("X-INTERNAL-KEY") String internalKey);
}