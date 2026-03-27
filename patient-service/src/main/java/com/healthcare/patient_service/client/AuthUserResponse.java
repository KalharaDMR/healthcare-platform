package com.healthcare.patient_service.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthUserResponse {

    @JsonProperty("userId")  // Map JSON "userId" to this field
    private String id;

    private String username;

    private String email;

    // Add other fields from auth-service response if needed
}