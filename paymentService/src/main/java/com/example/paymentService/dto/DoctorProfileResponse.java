package com.example.paymentService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorProfileResponse {
    private Long doctorId;
    private Long userId;
    private String username;
    private String doctorName;
    private String email;
    private String phoneNumber;
    private String status;
    private boolean approved;
    private LocalDateTime createdAt;
    private String specialization;
    private String licenseNumber;
    private String location;
}
