package com.healthcare.telemedicine_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUserSummary {
    private String username;
    private String email;
}
