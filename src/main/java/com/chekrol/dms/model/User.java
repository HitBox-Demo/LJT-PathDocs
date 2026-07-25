package com.chekrol.dms.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String status;
    private Long departmentId;
    private String departmentName;
    private final Set<String> roles = new LinkedHashSet<>();

    public User() {
    }

    public User(
            long id,
            String username,
            String fullName,
            String email,
            String status,
            Long departmentId,
            String departmentName,
            Set<String> roles
    ) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        setRoles(roles);
    }

    /**
     * Role comparison is deliberately normalised so values loaded as
     * "clerk", " CLERK " or "Clerk" are all treated as CLERK.
     */
    public boolean hasRole(String role) {
        String requiredRole = normaliseRole(role);

        if (requiredRole == null) {
            return false;
        }

        return roles.stream()
                .map(User::normaliseRole)
                .anyMatch(requiredRole::equals);
    }

    public void addRole(String role) {
        String normalisedRole = normaliseRole(role);

        if (normalisedRole != null) {
            roles.add(normalisedRole);
        }
    }

    public void removeRole(String role) {
        String normalisedRole = normaliseRole(role);

        if (normalisedRole == null) {
            return;
        }

        roles.removeIf(existingRole ->
                normalisedRole.equals(normaliseRole(existingRole))
        );
    }

    public void setRoles(Set<String> newRoles) {
        roles.clear();

        if (newRoles != null) {
            newRoles.forEach(this::addRole);
        }
    }

    /**
     * Re-normalises a user loaded from an older DAO/session implementation.
     */
    public void normaliseRoles() {
        Set<String> currentRoles = new LinkedHashSet<>(roles);
        setRoles(currentRoles);
    }

    public boolean canManageDocuments() {
        return hasRole("CLERK") || hasRole("SYSTEM_ADMIN");
    }

    public boolean canManageUsers() {
        return hasRole("SYSTEM_ADMIN") || hasRole("CLERK");
    }

    public boolean canApproveDocuments() {
        return hasRole("BOSS");
    }

    public boolean canViewDepartmentFolder() {
        return hasRole("DEPARTMENT_USER");
    }

    /* JavaBean getters for JSP Expression Language. */
    public boolean getCanManageDocuments() {
        return canManageDocuments();
    }

    public boolean getCanManageUsers() {
        return canManageUsers();
    }

    public boolean getCanApproveDocuments() {
        return canApproveDocuments();
    }

    public boolean getCanViewDepartmentFolder() {
        return canViewDepartmentFolder();
    }

    public String getPrimaryRole() {
        if (hasRole("SYSTEM_ADMIN")) {
            return "SYSTEM_ADMIN";
        }
        if (hasRole("CLERK")) {
            return "CLERK";
        }
        if (hasRole("BOSS")) {
            return "BOSS";
        }
        if (hasRole("DEPARTMENT_USER")) {
            return "DEPARTMENT_USER";
        }
        return "UNASSIGNED";
    }

    private static String normaliseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        return role.trim().toUpperCase(Locale.ROOT);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
