package com.example.paymentService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String status;
    private boolean approved;
    private LocalDateTime createdAt;
    private String phoneNumber;
}
