package com.healthcare.patient_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreatePatientRequest {
    private String userId;       // <-- add this field
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
}