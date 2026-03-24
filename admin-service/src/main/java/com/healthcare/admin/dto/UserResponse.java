package com.healthcare.admin.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String status;
    private boolean approved;
    private LocalDateTime createdAt;
    private Set<RoleResponse> roles;

    // Getters and setters (omitted for brevity, generate them)
}