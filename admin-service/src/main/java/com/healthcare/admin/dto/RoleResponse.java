package com.healthcare.admin.dto;
import lombok.Data;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}