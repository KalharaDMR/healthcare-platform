package com.healthcare.admin.dto;
import lombok.Data;
@Data
public class UpdateUserRequest {
    private String email;
    private String password;
    private String status;
    // getters and setters
}