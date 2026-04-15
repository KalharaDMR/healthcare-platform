package com.healthcare.telemedicine_service.controller;

import com.healthcare.telemedicine_service.dto.CreateSessionRequest;
import com.healthcare.telemedicine_service.dto.JoinSessionResponse;
import com.healthcare.telemedicine_service.service.TelemedicineService;
import com.healthcare.telemedicine_service.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class TelemedicineController {

    private final TelemedicineService service;
    private final JwtUtil jwtUtil;

    public TelemedicineController(TelemedicineService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    private String extractUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUsername(token);
    }

    private String extractRole(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractRoles(token).stream().findFirst().orElse("UNKNOWN");
    }

    @PostMapping("/sessions")
    public ResponseEntity<JoinSessionResponse> createSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody CreateSessionRequest request) {
        String username = extractUsername(authHeader);
        String role = extractRole(authHeader);
        JoinSessionResponse response = service.createSession(username, role, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{appointmentId}/join")
    public ResponseEntity<JoinSessionResponse> joinSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long appointmentId) {
        String username = extractUsername(authHeader);
        String role = extractRole(authHeader);
        JoinSessionResponse response = service.joinSession(username, role, appointmentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sessions/{appointmentId}")
    public ResponseEntity<String> endSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long appointmentId) {
        String username = extractUsername(authHeader);
        String role = extractRole(authHeader);
        service.endSession(username, role, appointmentId);
        return ResponseEntity.ok("Session ended");
    }
}