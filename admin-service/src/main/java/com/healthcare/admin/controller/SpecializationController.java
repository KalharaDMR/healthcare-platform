package com.healthcare.admin.controller;

import com.healthcare.admin.dto.SpecializationRequest;
import com.healthcare.admin.entity.Specialization;
import com.healthcare.admin.repository.SpecializationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class SpecializationController {

    private final SpecializationRepository specializationRepository;

    public SpecializationController(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    @GetMapping("/specializations")
    public List<String> getSpecializations() {
        return specializationRepository.findAll()
                .stream()
                .map(Specialization::getName)
                .collect(Collectors.toList());
    }

    @PostMapping("/specializations")
    public ResponseEntity<?> addSpecialization(@RequestBody SpecializationRequest request) {
        // Check if specialization already exists
        if (specializationRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Specialization already exists"));
        }
        Specialization newSpec = new Specialization(request.getName());
        specializationRepository.save(newSpec);
        return ResponseEntity.ok(Map.of("message", "Specialization added successfully", "name", request.getName()));
    }
}