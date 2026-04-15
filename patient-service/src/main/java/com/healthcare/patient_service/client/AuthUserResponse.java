package com.healthcare.patient_service.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class AuthUserResponse {
    private Long id;              // Changed from String to Long
    private String username;
    private String email;
    private String phoneNumber;
    private String status;
    private boolean approved;
    private LocalDateTime createdAt;
    private Set<Object> roles;    // roles from auth-service
}