package com.healthcare.admin.controller;

import com.healthcare.admin.client.AuthServiceClient;
import com.healthcare.admin.dto.RoleChangeRequest;
import com.healthcare.admin.dto.UpdateUserRequest;
import com.healthcare.admin.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthServiceClient authServiceClient;
    @Value("${internal.api.key}")
    private String internalApiKey;

    public AdminController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return authServiceClient.getAllUsers(internalApiKey);
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return authServiceClient.getUserById(id, internalApiKey);
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return authServiceClient.updateUser(id, request, internalApiKey);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        authServiceClient.deleteUser(id, internalApiKey);
    }

    @PutMapping("/users/{id}/role")
    public UserResponse changeUserRole(@PathVariable Long id, @RequestBody RoleChangeRequest request) {
        return authServiceClient.changeUserRole(id, request, internalApiKey);
    }

    @PutMapping("/users/{id}/approve")
    public void approveDoctor(@PathVariable Long id) {
        authServiceClient.approveDoctor(id, internalApiKey);
    }
}