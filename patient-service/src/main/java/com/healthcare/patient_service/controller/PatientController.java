package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.client.AuthClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final AuthClient authClient;

    @Value("${internal.api.key}")
    private String apiKey;

    public PatientController(AuthClient authClient) {
        this.authClient = authClient;
    }

    @GetMapping("/{userId}")
    public Object getPatientProfile(@PathVariable Long userId) {
        return authClient.getUserById(userId, apiKey);
    }

    @GetMapping
    public List<Object> getAllUsers() {
        return authClient.getAllUsers(apiKey);
    }
}
