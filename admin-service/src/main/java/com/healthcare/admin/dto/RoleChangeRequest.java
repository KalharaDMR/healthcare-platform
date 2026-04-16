package com.healthcare.admin.dto;
import lombok.Data;

@Data
public class RoleChangeRequest {
    private String roleName;
    // getters and setters
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

}