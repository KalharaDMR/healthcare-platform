package com.healthcare.doctor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorVerificationRequest {
    private boolean approved;
    private String remarks;
}