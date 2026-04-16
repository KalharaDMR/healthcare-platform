package com.healthcare.doctor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DoctorController {

    @GetMapping("/doctor/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Doctor service is running");
    }
}