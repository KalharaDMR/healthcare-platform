package com.healthcare.doctor.dto;

import com.healthcare.doctor.entity.DoctorVerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DoctorResponse {
    private Long id;
    private Long authUserId;
    private String fullName;
    private String licenseNumber;
    private String primarySpecialization;
    private String secondarySpecialization;
    private String location;
    private String hospitalName;
    private String bio;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private DoctorVerificationStatus verificationStatus;
    private String verificationRemarks;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DoctorAvailabilitySlotResponse> availabilitySlots;
}