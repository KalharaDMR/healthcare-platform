package com.healthcare.doctor.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorUpdateRequest {
    private String fullName;
    private String primarySpecialization;
    private String secondarySpecialization;
    private String location;
    private String hospitalName;
    private String bio;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private Boolean active;
}