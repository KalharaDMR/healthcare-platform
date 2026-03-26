package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.SpecializationRequest;
import com.healthcare.doctor.dto.SpecializationResponse;
import com.healthcare.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors/specializations")
@RequiredArgsConstructor
public class DoctorSpecializationController {

    private final DoctorService doctorService;

    @PostMapping
    public SpecializationResponse create(@Valid @RequestBody SpecializationRequest request) {
        return doctorService.createSpecialization(request);
    }

    @GetMapping
    public List<SpecializationResponse> getAll(
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return doctorService.getAllSpecializations(activeOnly);
    }

    @GetMapping("/{id}")
    public SpecializationResponse getById(@PathVariable Long id) {
        return doctorService.getSpecializationById(id);
    }

    @PutMapping("/{id}")
    public SpecializationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody SpecializationRequest request
    ) {
        return doctorService.updateSpecialization(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        doctorService.deleteSpecialization(id);
    }
}