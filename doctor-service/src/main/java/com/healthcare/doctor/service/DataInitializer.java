package com.healthcare.doctor.service;

import com.healthcare.doctor.entity.DoctorSpecialization;
import com.healthcare.doctor.repository.DoctorSpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DoctorSpecializationRepository specializationRepository;

    @Override
    public void run(String... args) {
        List<String> defaultSpecializations = List.of(
                "Physician",
                "Cardiologist",
                "Dermatologist",
                "Pediatrician",
                "Neurologist",
                "Orthopedic",
                "Psychiatrist",
                "Gynecologist"
        );

        for (String name : defaultSpecializations) {
            if (!specializationRepository.existsByNameIgnoreCase(name)) {
                DoctorSpecialization specialization = new DoctorSpecialization();
                specialization.setName(name);
                specialization.setDescription(name + " specialization");
                specialization.setActive(true);
                specializationRepository.save(specialization);
            }
        }
    }
}