package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.AvailabilityRequest;
import com.healthcare.doctor.entity.AvailabilitySlot;
import com.healthcare.doctor.repository.AvailabilityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilityRepository repository;

    public AvailabilityService(AvailabilityRepository repository) {
        this.repository = repository;
    }

    public AvailabilitySlot create(String doctorUsername, AvailabilityRequest request) {
        validateRequest(doctorUsername, request, null);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setDoctorUsername(doctorUsername);
        slot.setDate(request.getDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setHospital(request.getHospital().trim());
        slot.setAvailable(request.getAvailable() == null || request.getAvailable());

        return repository.save(slot);
    }

    public List<AvailabilitySlot> getByDoctor(String doctorUsername, LocalDate date, String hospital) {
        List<AvailabilitySlot> slots = repository.findByDoctorUsernameOrderByDateAscStartTimeAsc(doctorUsername);

        if (date != null) {
            slots = slots.stream()
                    .filter(slot -> slot.getDate().equals(date))
                    .toList();
        }

        if (hospital != null && !hospital.isBlank()) {
            String hospitalFilter = hospital.trim().toLowerCase();
            slots = slots.stream()
                    .filter(slot -> slot.getHospital() != null
                            && slot.getHospital().trim().toLowerCase().equals(hospitalFilter))
                    .toList();
        }

        return slots;
    }

    public AvailabilitySlot getOne(String doctorUsername, Long slotId) {
        return repository.findByIdAndDoctorUsername(slotId, doctorUsername)
                .orElseThrow(() -> new RuntimeException("Availability slot not found"));
    }

    public AvailabilitySlot update(String doctorUsername, Long slotId, AvailabilityRequest request) {
        AvailabilitySlot existing = repository.findByIdAndDoctorUsername(slotId, doctorUsername)
                .orElseThrow(() -> new RuntimeException("Availability slot not found"));

        validateRequest(doctorUsername, request, slotId);

        existing.setDate(request.getDate());
        existing.setStartTime(request.getStartTime());
        existing.setEndTime(request.getEndTime());
        existing.setHospital(request.getHospital().trim());

        if (request.getAvailable() != null) {
            existing.setAvailable(request.getAvailable());
        }

        return repository.save(existing);
    }

    public AvailabilitySlot patch(String doctorUsername, Long slotId, AvailabilityRequest request) {
        AvailabilitySlot existing = repository.findByIdAndDoctorUsername(slotId, doctorUsername)
                .orElseThrow(() -> new RuntimeException("Availability slot not found"));

        if (request.getDate() != null) {
            existing.setDate(request.getDate());
        }

        if (request.getStartTime() != null) {
            existing.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            existing.setEndTime(request.getEndTime());
        }

        if (request.getHospital() != null && !request.getHospital().isBlank()) {
            existing.setHospital(request.getHospital().trim());
        }

        if (request.getAvailable() != null) {
            existing.setAvailable(request.getAvailable());
        }

        validateExistingSlot(doctorUsername, existing, slotId);

        return repository.save(existing);
    }

    public void delete(String doctorUsername, Long slotId) {
        AvailabilitySlot existing = repository.findByIdAndDoctorUsername(slotId, doctorUsername)
                .orElseThrow(() -> new RuntimeException("Availability slot not found"));

        repository.delete(existing);
    }

    private void validateRequest(String doctorUsername, AvailabilityRequest request, Long excludeId) {
        if (request.getDate() == null) {
            throw new RuntimeException("date is required");
        }

        if (request.getStartTime() == null) {
            throw new RuntimeException("startTime is required");
        }

        if (request.getEndTime() == null) {
            throw new RuntimeException("endTime is required");
        }

        if (request.getHospital() == null || request.getHospital().isBlank()) {
            throw new RuntimeException("hospital is required");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("endTime must be after startTime");
        }

        boolean overlaps = repository.existsOverlappingSlot(
                doctorUsername,
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                excludeId
        );

        if (overlaps) {
            throw new RuntimeException("Overlapping availability slot already exists");
        }
    }

    private void validateExistingSlot(String doctorUsername, AvailabilitySlot slot, Long excludeId) {
        if (slot.getDate() == null) {
            throw new RuntimeException("date is required");
        }

        if (slot.getStartTime() == null) {
            throw new RuntimeException("startTime is required");
        }

        if (slot.getEndTime() == null) {
            throw new RuntimeException("endTime is required");
        }

        if (slot.getHospital() == null || slot.getHospital().isBlank()) {
            throw new RuntimeException("hospital is required");
        }

        if (!slot.getEndTime().isAfter(slot.getStartTime())) {
            throw new RuntimeException("endTime must be after startTime");
        }

        boolean overlaps = repository.existsOverlappingSlot(
                doctorUsername,
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                excludeId
        );

        if (overlaps) {
            throw new RuntimeException("Overlapping availability slot already exists");
        }
    }
}