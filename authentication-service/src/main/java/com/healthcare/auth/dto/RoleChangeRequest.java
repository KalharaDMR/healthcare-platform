package com.healthcare.auth.dto;

public class RoleChangeRequest {
    private String roleName;

    public RoleChangeRequest() {}

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}