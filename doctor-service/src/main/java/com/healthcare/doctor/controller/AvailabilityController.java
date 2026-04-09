package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.AvailabilityRequest;
import com.healthcare.doctor.entity.AvailabilitySlot;
import com.healthcare.doctor.service.AvailabilityService;
import com.healthcare.doctor.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctor/availability")
public class AvailabilityController {

    private final AvailabilityService service;
    private final JwtUtil jwtUtil;

    public AvailabilityController(AvailabilityService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<AvailabilitySlot> createSlot(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody AvailabilityRequest request) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(service.create(username, request));
    }

    @GetMapping
    public ResponseEntity<List<AvailabilitySlot>> getSlots(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String hospital) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(service.getByDoctor(username, date, hospital));
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<AvailabilitySlot> getSlot(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long slotId) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(service.getOne(username, slotId));
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<AvailabilitySlot> updateSlot(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long slotId,
            @Valid @RequestBody AvailabilityRequest request) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(service.update(username, slotId, request));
    }

    @PatchMapping("/{slotId}")
    public ResponseEntity<AvailabilitySlot> patchSlot(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long slotId,
            @RequestBody AvailabilityRequest request) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(service.patch(username, slotId, request));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<String> deleteSlot(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long slotId) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        service.delete(username, slotId);
        return ResponseEntity.ok("Availability slot deleted successfully");
    }

    private String extractUsernameFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new RuntimeException("Missing Authorization header");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header must start with Bearer");
        }

        String token = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username == null || username.isBlank()) {
            throw new RuntimeException("Invalid or expired token");
        }

        return username;
    }

    @GetMapping("/public/{doctorUsername}")
    public ResponseEntity<List<AvailabilitySlot>> getDoctorAvailabilityPublic(
            @PathVariable String doctorUsername,
            @RequestParam(required = false) LocalDate date) {

        List<AvailabilitySlot> slots = service.getByDoctor(doctorUsername, date, null)
                .stream()
                .filter(AvailabilitySlot::isAvailable)
                .toList();

        return ResponseEntity.ok(slots);
    }
}