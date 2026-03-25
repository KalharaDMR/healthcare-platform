package com.healthcare.auth.dto;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String name;
    private String role;
    private String specialization;
    public RegisterRequest() {}

    public void setSpecialization(String specialization) { this.specialization = specialization;}
    public String getSpecialization(){return specialization;}
    public String getUsername() {
        return username;
    }
    public void setName(String name){this.name = name;}
    public String getName(){return name;}
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}