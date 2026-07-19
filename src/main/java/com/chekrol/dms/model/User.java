package com.chekrol.dms.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class User implements Serializable {
    private long id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String status;
    private Long departmentId;
    private String departmentName;
    private final Set<String> roles = new LinkedHashSet<>();

    public User() {}

    public User(long id, String username, String fullName, String email, String status,
                Long departmentId, String departmentName, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        if (roles != null) this.roles.addAll(roles);
    }

    public boolean hasRole(String role) { return role != null && roles.contains(role.toUpperCase()); }
    public String getPrimaryRole() {
        if (hasRole("SYSTEM_ADMIN")) return "SYSTEM_ADMIN";
        if (hasRole("CLERK")) return "CLERK";
        if (hasRole("BOSS")) return "BOSS";
        return "DEPARTMENT_USER";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Set<String> getRoles() { return roles; }
}
