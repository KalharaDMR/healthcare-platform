package com.healthcare.doctor.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DoctorSearchResponse {
    private Long id;
    private String fullName;
    private String primarySpecialization;
    private String secondarySpecialization;
    private String location;
    private String hospitalName;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private List<DoctorAvailabilitySlotResponse> matchingSlots;
}