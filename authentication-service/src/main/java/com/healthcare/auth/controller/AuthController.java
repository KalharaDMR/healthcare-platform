package com.healthcare.auth.controller;

import com.healthcare.auth.dto.DoctorProfileResponse;
import com.healthcare.auth.dto.DoctorProfileUpdateRequest;
import com.healthcare.auth.dto.DoctorRegisterRequest;
import com.healthcare.auth.dto.LoginRequest;
import com.healthcare.auth.dto.RegisterRequest;
import com.healthcare.auth.entity.User;
import com.healthcare.auth.service.AuthService;
import com.healthcare.auth.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return authService.registerUser(request);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        try {
            User user = authService.registerDoctor(request);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Map<String, Object> response = Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Login successful",
                    "username", userDetails.getUsername(),
                    "token", token
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            Map<String, Object> errorResponse = Map.of(
                    "status", HttpStatus.UNAUTHORIZED.value(),
                    "message", ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (DisabledException ex) {
            Map<String, Object> errorResponse = Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "message", "Account is disabled"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (Exception ex) {
            Map<String, Object> errorResponse = Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "message", "Something went wrong: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/profile/doctor")
    public ResponseEntity<DoctorProfileResponse> getDoctorProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        DoctorProfileResponse response = authService.getDoctorProfileByUsername(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/doctor")
    public ResponseEntity<DoctorProfileResponse> updateDoctorProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody DoctorProfileUpdateRequest request) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        DoctorProfileResponse response = authService.updateDoctorProfileByUsername(username, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile/doctor")
    public ResponseEntity<DoctorProfileResponse> patchDoctorProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody DoctorProfileUpdateRequest request) {

        String username = extractUsernameFromAuthorizationHeader(authorizationHeader);
        DoctorProfileResponse response = authService.updateDoctorProfileByUsername(username, request);
        return ResponseEntity.ok(response);
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
}