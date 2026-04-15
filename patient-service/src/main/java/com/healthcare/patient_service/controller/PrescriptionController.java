package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.dto.CreatePrescriptionRequest;
import com.healthcare.patient_service.dto.PrescriptionResponse;
import com.healthcare.patient_service.entity.Prescription;
import com.healthcare.patient_service.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService service;

    public PrescriptionController(PrescriptionService service) {
        this.service = service;
    }

    @PostMapping
    public PrescriptionResponse create(@RequestBody CreatePrescriptionRequest request) {
        Prescription p = new Prescription();
        p.setUserId(request.getUserId());
        p.setDoctorId(request.getDoctorId());
        p.setMedications(request.getMedications());
        p.setNotes(request.getNotes());
        return service.toResponse(service.save(p));
    }

    @GetMapping
    public List<PrescriptionResponse> getAll() {
        return service.toResponseList(service.getAll());
    }

    @GetMapping("/{id}")
    public PrescriptionResponse getById(@PathVariable Long id) {
        return service.toResponse(service.getById(id));
    }

    @GetMapping("/user/{userId}")
    public List<PrescriptionResponse> getByUser(@PathVariable Long userId) {
        return service.toResponseList(service.getByUserId(userId));
    }
}