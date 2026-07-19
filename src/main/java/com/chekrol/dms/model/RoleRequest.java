package com.chekrol.dms.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RoleRequest implements Serializable {
    private long id;
    private long userId;
    private String userName;
    private String currentRole;
    private String requestedRole;
    private long requestedById;
    private String requestedByName;
    private String status;
    private LocalDateTime requestedAt;
    private String remarks;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
    public String getRequestedRole() { return requestedRole; }
    public void setRequestedRole(String requestedRole) { this.requestedRole = requestedRole; }
    public long getRequestedById() { return requestedById; }
    public void setRequestedById(long requestedById) { this.requestedById = requestedById; }
    public String getRequestedByName() { return requestedByName; }
    public void setRequestedByName(String requestedByName) { this.requestedByName = requestedByName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
