package com.healthcare.auth.controller;

import com.healthcare.auth.dto.*;
import com.healthcare.auth.entity.User;
import com.healthcare.auth.repository.UserRepository;
import com.healthcare.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal")
public class InternalUserController {

    private final UserRepository userRepository;
    private final AuthService authService;
    @Value("${internal.api.key}")
    private String internalApiKey;

    public InternalUserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    // Simple API key check (you can add a filter later)
    private void validateApiKey(String apiKey) {
        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            throw new RuntimeException("Unauthorized");
        }
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers(@RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey) {
        validateApiKey(apiKey);
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id,
                                    @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey) {
        validateApiKey(apiKey);
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @RequestBody UpdateUserRequest request,
                                   @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey) {
        validateApiKey(apiKey);
        User updated = authService.updateUser(id, request);
        return toResponse(updated);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey) {
        validateApiKey(apiKey);
        authService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/role")
    public UserResponse changeUserRole(@PathVariable Long id,
                                       @RequestBody RoleChangeRequest request,
                                       @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey) {
        validateApiKey(apiKey);
        User updated = authService.changeUserRole(id, request);
        return toResponse(updated);
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveDoctor(@PathVariable Long id,
                                           @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey) {
        validateApiKey(apiKey);
        authService.approveDoctor(id);
        return ResponseEntity.ok().build();
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setApproved(user.isApproved());
        response.setCreatedAt(user.getCreatedAt());
        response.setRoles(user.getRoles());
        return response;
    }
}