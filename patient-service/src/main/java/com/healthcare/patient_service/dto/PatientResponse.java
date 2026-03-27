package com.healthcare.patient_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientResponse {
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
}