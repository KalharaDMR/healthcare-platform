package com.example.aisymptom_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PatientRequest {
    @NotNull
    private Integer age;

    @NotBlank
    private String gender;

    @NotNull
    private List<@NotBlank String> symptoms;
}
