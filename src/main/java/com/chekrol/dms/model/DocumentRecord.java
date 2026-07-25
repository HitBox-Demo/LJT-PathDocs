package com.chekrol.dms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DocumentRecord implements Serializable {
    private long id;
    private String documentCode;
    private String title;
    private String referenceNo;
    private String sender;
    private LocalDate dateReceived;
    private String category;
    private String priority;
    private String description;
    private Long destinationDepartmentId;
    private String destinationDepartmentName;
    private Long bossId;
    private String bossName;
    private String status;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDate dueDate;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private int daysPending;
    private String primaryFileName;
    private String primaryFilePath;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDocumentCode() { return documentCode; }
    public void setDocumentCode(String documentCode) { this.documentCode = documentCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getDestinationDepartmentId() { return destinationDepartmentId; }
    public void setDestinationDepartmentId(Long destinationDepartmentId) { this.destinationDepartmentId = destinationDepartmentId; }
    public String getDestinationDepartmentName() { return destinationDepartmentName; }
    public void setDestinationDepartmentName(String destinationDepartmentName) { this.destinationDepartmentName = destinationDepartmentName; }
    public Long getBossId() { return bossId; }
    public void setBossId(Long bossId) { this.bossId = bossId; }
    public String getBossName() { return bossName; }
    public void setBossName(String bossName) { this.bossName = bossName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public int getDaysPending() { return daysPending; }
    public void setDaysPending(int daysPending) { this.daysPending = daysPending; }
    public String getPrimaryFileName() { return primaryFileName; }
    public void setPrimaryFileName(String primaryFileName) { this.primaryFileName = primaryFileName; }
    public String getPrimaryFilePath() { return primaryFilePath; }
    public void setPrimaryFilePath(String primaryFilePath) { this.primaryFilePath = primaryFilePath; }
}
