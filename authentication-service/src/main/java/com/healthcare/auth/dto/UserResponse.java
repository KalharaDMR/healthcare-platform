package com.healthcare.auth.dto;

import com.healthcare.auth.entity.Role;
import java.time.LocalDateTime;
import java.util.Set;

public class UserResponse {
    private Long id;
    private String name;
    private String specialization;
    private String username;
    private String email;
    private String status;
    private boolean approved;
    private LocalDateTime createdAt;
    private Set<Role> roles;
    private String phoneNumber;

    public UserResponse() {}

    public void setName(String name){this.name = name;}

    public String getName() {return name;}

    public void setSpecialization(String specialization){this.specialization = specialization;}

    public String getSpecialization(){return specialization;}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}