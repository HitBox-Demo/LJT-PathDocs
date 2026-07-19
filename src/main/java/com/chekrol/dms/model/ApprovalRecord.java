package com.chekrol.dms.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ApprovalRecord implements Serializable {
    private long id;
    private long documentId;
    private String documentCode;
    private String documentTitle;
    private long approverId;
    private String approverName;
    private String decision;
    private String remarks;
    private String previousDepartment;
    private String finalDepartment;
    private LocalDateTime decisionAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getDocumentId() { return documentId; }
    public void setDocumentId(long documentId) { this.documentId = documentId; }
    public String getDocumentCode() { return documentCode; }
    public void setDocumentCode(String documentCode) { this.documentCode = documentCode; }
    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }
    public long getApproverId() { return approverId; }
    public void setApproverId(long approverId) { this.approverId = approverId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getPreviousDepartment() { return previousDepartment; }
    public void setPreviousDepartment(String previousDepartment) { this.previousDepartment = previousDepartment; }
    public String getFinalDepartment() { return finalDepartment; }
    public void setFinalDepartment(String finalDepartment) { this.finalDepartment = finalDepartment; }
    public LocalDateTime getDecisionAt() { return decisionAt; }
    public void setDecisionAt(LocalDateTime decisionAt) { this.decisionAt = decisionAt; }
}
