package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.AppointmentCreateRequest;
import com.healthcare.appointment.dto.AppointmentRescheduleRequest;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.entity.AppointmentStatus;
import com.healthcare.appointment.service.AppointmentService;
import com.healthcare.appointment.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final JwtUtil jwtUtil;

    public AppointmentController(AppointmentService appointmentService, JwtUtil jwtUtil) {
        this.appointmentService = appointmentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(
            @RequestParam String username,
            @Valid @RequestBody AppointmentCreateRequest request,@RequestParam Boolean isEnableVideo) {
        return ResponseEntity.ok(appointmentService.book(username, request,isEnableVideo));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> myAppointments(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(required = false) AppointmentStatus status) {

        String token = extractToken(authorizationHeader);
        ensureRole(token, "ROLE_PATIENT");

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(appointmentService.getMyAppointments(username, status));
    }

    @GetMapping("/my/{appointmentId}")
    public ResponseEntity<AppointmentResponse> myAppointmentById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long appointmentId) {

        String token = extractToken(authorizationHeader);
        ensureRole(token, "ROLE_PATIENT");

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(appointmentService.getMyAppointmentById(appointmentId, username));
    }

    @GetMapping("/myAppointment")
    public ResponseEntity<AppointmentResponse> myAppointment(
            @RequestParam Long appointmentId) {
            AppointmentResponse appointmentResponse = appointmentService.getAppointment(appointmentId);
            return  ResponseEntity.ok(appointmentResponse);
    }



    @PutMapping("/my/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse> cancelMyAppointment(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelMyAppointment(appointmentId));
    }

    @PutMapping("/my/{appointmentId}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleMyAppointment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long appointmentId,
            @Valid @RequestBody AppointmentRescheduleRequest request) {

        String token = extractToken(authorizationHeader);
        ensureRole(token, "ROLE_PATIENT");

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(
                appointmentService.rescheduleMyAppointment(appointmentId, username, request)
        );
    }

    @GetMapping("/doctor")
    public ResponseEntity<List<AppointmentResponse>> doctorAppointments(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(required = false) AppointmentStatus status) {

        String token = extractToken(authorizationHeader);
        ensureRole(token, "ROLE_DOCTOR");

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(username, status));
    }

    @PutMapping("/doctor/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateDoctorStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status) {

        String token = extractToken(authorizationHeader);
        ensureRole(token, "ROLE_DOCTOR");

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(
                appointmentService.updateStatusForDoctor(appointmentId, username, status)
        );
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new RuntimeException("Missing Authorization header");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header must start with Bearer");
        }

        return authorizationHeader.substring(7);
    }

    private void ensureRole(String token, String requiredRole) {
        List<String> roles = jwtUtil.extractRoles(token);
        if (!roles.contains(requiredRole)) {
            throw new RuntimeException("Access denied");
        }
    }
}