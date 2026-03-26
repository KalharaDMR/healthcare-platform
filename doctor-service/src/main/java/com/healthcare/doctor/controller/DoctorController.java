package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.*;
import com.healthcare.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/register")
    public DoctorResponse registerDoctor(@Valid @RequestBody DoctorRegistrationRequest request) {
        return doctorService.registerDoctor(request);
    }

    @GetMapping
    public List<DoctorResponse> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{doctorId}")
    public DoctorResponse getDoctor(@PathVariable Long doctorId) {
        return doctorService.getDoctor(doctorId);
    }

    @PutMapping("/{doctorId}")
    public DoctorResponse updateDoctor(@PathVariable Long doctorId,
                                       @RequestBody DoctorUpdateRequest request) {
        return doctorService.updateDoctor(doctorId, request);
    }

    @DeleteMapping("/{doctorId}")
    public void deleteDoctor(@PathVariable Long doctorId) {
        doctorService.deleteDoctor(doctorId);
    }

    @GetMapping("/search")
    public List<DoctorSearchResponse> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String availableFrom,
            @RequestParam(required = false) String availableTo
    ) {
        return doctorService.searchDoctors(specialization, location, availableFrom, availableTo);
    }

    @PostMapping("/{doctorId}/schedule/slots")
    public DoctorAvailabilitySlotResponse addAvailabilitySlot(
            @PathVariable Long doctorId,
            @Valid @RequestBody AvailabilitySlotRequest request
    ) {
        return doctorService.addAvailabilitySlot(doctorId, request);
    }

    @GetMapping("/{doctorId}/schedule/slots")
    public List<DoctorAvailabilitySlotResponse> getAvailabilitySlots(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "false") boolean availableOnly
    ) {
        return doctorService.getAvailabilitySlots(doctorId, availableOnly);
    }

    @PutMapping("/{doctorId}/schedule/slots/{slotId}")
    public DoctorAvailabilitySlotResponse updateAvailabilitySlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotId,
            @Valid @RequestBody AvailabilitySlotUpdateRequest request
    ) {
        return doctorService.updateAvailabilitySlot(doctorId, slotId, request);
    }

    @DeleteMapping("/{doctorId}/schedule/slots/{slotId}")
    public void deleteAvailabilitySlot(@PathVariable Long doctorId, @PathVariable Long slotId) {
        doctorService.deleteAvailabilitySlot(doctorId, slotId);
    }
}