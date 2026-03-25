package com.healthcare.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorRegistrationRequest {

    @NotNull
    private Long authUserId;

    @NotBlank
    private String fullName;

    @NotBlank
    private String licenseNumber;

    @NotBlank
    private String primarySpecialization;

    private String secondarySpecialization;

    @NotBlank
    private String location;

    private String hospitalName;
    private String bio;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
}