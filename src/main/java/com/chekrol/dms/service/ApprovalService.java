package com.chekrol.dms.service;

import com.chekrol.dms.dao.ApprovalDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;

import java.sql.SQLException;
import java.util.Set;

public class ApprovalService {
    private static final Set<String> DECISIONS = Set.of("APPROVE", "REJECT");
    private final ApprovalDAO approvalDAO = new ApprovalDAO();

    public void decide(
            User boss,
            long documentId,
            String decision,
            String remarks,
            Long newDepartmentId
    ) throws SQLException {
        if (boss == null || !boss.hasRole("BOSS")) {
            throw new IllegalArgumentException("Only a boss can process an approval.");
        }
        if (!DECISIONS.contains(decision)) {
            throw new IllegalArgumentException("Unsupported approval decision.");
        }
        if ("REJECT".equals(decision) && (remarks == null || remarks.isBlank())) {
            throw new IllegalArgumentException("A return reason is required.");
        }

        if (AppConfig.isDemoMode()) {
            DemoData.action(boss, documentId, decision, remarks, newDepartmentId);
            return;
        }
        approvalDAO.decide(boss, documentId, decision, remarks, newDepartmentId);
    }
}
