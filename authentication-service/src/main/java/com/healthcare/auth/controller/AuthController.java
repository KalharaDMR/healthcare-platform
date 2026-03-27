package com.healthcare.auth.controller;

import com.healthcare.auth.dto.DoctorRegisterRequest;
import com.healthcare.auth.dto.LoginRequest;
import com.healthcare.auth.dto.RegisterRequest;
import com.healthcare.auth.entity.User;
import com.healthcare.auth.service.AuthService;
import com.healthcare.auth.util.JwtUtil;
import org.springframework.http.HttpStatus;                     // ✅ HttpStatus
import org.springframework.security.authentication.BadCredentialsException; // ✅ BadCredentialsException
import org.springframework.security.authentication.DisabledException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
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
            // 1️⃣ Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2️⃣ Set authentication in context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3️⃣ Generate JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // 4️⃣ Build success response (JSON)
            Map<String, Object> response = Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Login successful",
                    "username", userDetails.getUsername(),
                    "token", token
            );

            return ResponseEntity.ok(response); // 200 OK

        } catch (BadCredentialsException ex) {
            // Invalid username/password
            Map<String, Object> errorResponse = Map.of(
                    "status", HttpStatus.UNAUTHORIZED.value(),
                    "message", ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (DisabledException ex) {
            // Account disabled
            Map<String, Object> errorResponse = Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "message", "Account is disabled"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception ex) {
            // Other errors
            Map<String, Object> errorResponse = Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "message", "Something went wrong: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}