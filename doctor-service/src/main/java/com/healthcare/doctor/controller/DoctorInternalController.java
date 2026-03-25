package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.DoctorResponse;
import com.healthcare.doctor.dto.DoctorVerificationRequest;
import com.healthcare.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/doctors")
@RequiredArgsConstructor
public class DoctorInternalController {

    private final DoctorService doctorService;

    @Value("${internal.api.key}")
    private String internalApiKey;

    private void validateInternalKey(String key) {
        if (key == null || !key.equals(internalApiKey)) {
            throw new IllegalArgumentException("Invalid internal API key");
        }
    }

    @GetMapping("/pending")
    public List<DoctorResponse> getPendingDoctors(
            @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey
    ) {
        validateInternalKey(apiKey);
        return doctorService.getPendingDoctors();
    }

    @PutMapping("/{doctorId}/verify")
    public DoctorResponse verifyDoctor(
            @PathVariable Long doctorId,
            @RequestBody DoctorVerificationRequest request,
            @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey
    ) {
        validateInternalKey(apiKey);
        return doctorService.verifyDoctor(doctorId, request);
    }
}