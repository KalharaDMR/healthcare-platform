package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.dto.CreatePrescriptionRequest;
import com.healthcare.patient_service.dto.PrescriptionResponse;
import com.healthcare.patient_service.entity.Prescription;
import com.healthcare.patient_service.client.AuthClient;
import org.springframework.beans.factory.annotation.Value;
import com.healthcare.patient_service.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService service;
    private final AuthClient authClient;
    @Value("${internal.api.key}")
    private String internalApiKey;

    public PrescriptionController(PrescriptionService service, AuthClient authClient) {
        this.service = service;
        this.authClient = authClient;
    }

    @PostMapping
    public PrescriptionResponse create(@RequestBody CreatePrescriptionRequest request,
                                       @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
                                       @RequestHeader(value = "X-INTERNAL-KEY", required = false) String internalKey) {
        ensureDoctorOrInternalAccess(rolesHeader, internalKey);

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

    @GetMapping("/me")
    public List<PrescriptionResponse> getMyPrescriptions(
            @RequestHeader(value = "X-User-Id", required = false) String username) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Missing user header");
        }
        Long userId = authClient.getUserByUsername(username).getId();
        return service.toResponseList(service.getByUserId(userId));
    }

    @GetMapping("/doctor/user/{userId}")
    public List<PrescriptionResponse> getByUserForDoctor(@PathVariable Long userId,
                                                         @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
                                                         @RequestHeader(value = "X-INTERNAL-KEY", required = false) String internalKey) {
        ensureDoctorOrInternalAccess(rolesHeader, internalKey);
        return service.toResponseList(service.getByUserId(userId));
    }

    private void ensureDoctorOrInternalAccess(String rolesHeader, String internalKey) {
        if (internalApiKey.equals(internalKey)) return;
        if (rolesHeader == null || rolesHeader.isBlank()) {
            throw new RuntimeException("Doctor access required");
        }

        boolean isDoctor = List.of(rolesHeader.split(",")).stream()
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .map(r -> r.toUpperCase(Locale.ROOT))
                .anyMatch("DOCTOR"::equals);

        if (!isDoctor) {
            throw new RuntimeException("Doctor access required");
        }
    }
}