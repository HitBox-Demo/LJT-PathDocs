package com.chekrol.dms.dao;

import com.chekrol.dms.model.ApprovalRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApprovalDAO {
    public void decide(
            User boss,
            long documentId,
            String decision,
            String remarks,
            Long newDepartmentId
    ) throws SQLException {
        String lockSql = """
                SELECT doc.created_by, doc.destination_department_id, doc.document_code,
                       dept.department_name
                FROM dms_document doc
                JOIN dms_department dept ON dept.department_id=doc.destination_department_id
                WHERE doc.document_id=? AND doc.boss_id=? AND doc.status='PENDING_APPROVAL'
                FOR UPDATE
                """;
        String approveSql = """
                UPDATE dms_document
                SET status='ROUTED', destination_department_id=?, approved_at=SYSTIMESTAMP,
                    rejection_reason=NULL, updated_at=SYSTIMESTAMP
                WHERE document_id=?
                """;
        String rejectSql = """
                UPDATE dms_document
                SET status='RETURNED_FOR_CORRECTION', destination_department_id=?,
                    rejection_reason=?, updated_at=SYSTIMESTAMP
                WHERE document_id=?
                """;
        String approvalSql = """
                INSERT INTO dms_approval(
                    document_id, approver_id, decision, remarks,
                    previous_department_id, new_department_id
                ) VALUES(?,?,?,?,?,?)
                """;
        String historySql = """
                INSERT INTO dms_document_history(
                    document_id, user_id, action, old_value, new_value, remarks
                ) VALUES(?,?,?,'PENDING_APPROVAL',?,?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long creatorId;
                long oldDepartmentId;
                String documentCode;
                String oldDepartmentName;
                try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
                    statement.setLong(1, documentId);
                    statement.setLong(2, boss.getId());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Document is not pending for this boss.");
                        }
                        creatorId = resultSet.getLong(1);
                        oldDepartmentId = resultSet.getLong(2);
                        documentCode = resultSet.getString(3);
                        oldDepartmentName = resultSet.getString(4);
                    }
                }

                long finalDepartmentId = newDepartmentId == null
                        ? oldDepartmentId
                        : newDepartmentId;
                String finalDepartmentName = departmentName(connection, finalDepartmentId);

                if ("APPROVE".equals(decision)) {
                    try (PreparedStatement statement = connection.prepareStatement(approveSql)) {
                        statement.setLong(1, finalDepartmentId);
                        statement.setLong(2, documentId);
                        statement.executeUpdate();
                    }
                } else if ("REJECT".equals(decision)) {
                    if (remarks == null || remarks.isBlank()) {
                        throw new SQLException("A return reason is required.");
                    }
                    try (PreparedStatement statement = connection.prepareStatement(rejectSql)) {
                        statement.setLong(1, finalDepartmentId);
                        statement.setString(2, remarks);
                        statement.setLong(3, documentId);
                        statement.executeUpdate();
                    }
                } else {
                    throw new SQLException("Unsupported decision.");
                }

                try (PreparedStatement statement = connection.prepareStatement(approvalSql)) {
                    statement.setLong(1, documentId);
                    statement.setLong(2, boss.getId());
                    statement.setString(3, decision);
                    statement.setString(4, remarks);
                    statement.setLong(5, oldDepartmentId);
                    statement.setLong(6, finalDepartmentId);
                    statement.executeUpdate();
                }

                String newStatus = "APPROVE".equals(decision)
                        ? "ROUTED"
                        : "RETURNED_FOR_CORRECTION";
                try (PreparedStatement statement = connection.prepareStatement(historySql)) {
                    statement.setLong(1, documentId);
                    statement.setLong(2, boss.getId());
                    statement.setString(3, decision);
                    statement.setString(4, newStatus);
                    statement.setString(5, remarks);
                    statement.executeUpdate();
                }

                if (oldDepartmentId != finalDepartmentId) {
                    DocumentDAO.insertNotification(
                            connection,
                            creatorId,
                            "DESTINATION_CHANGED",
                            documentCode + " was redirected from " + oldDepartmentName
                                    + " to " + finalDepartmentName + " by " + boss.getFullName() + ".",
                            documentId
                    );
                }

                DocumentDAO.insertNotification(
                        connection,
                        creatorId,
                        "APPROVE".equals(decision) ? "APPROVED" : "RETURNED",
                        "APPROVE".equals(decision)
                                ? documentCode + " was approved and routed to " + finalDepartmentName + "."
                                : documentCode + " was returned for correction.",
                        documentId
                );

                if ("APPROVE".equals(decision)) {
                    notifyDepartmentUsers(
                            connection, finalDepartmentId, documentId, documentCode, finalDepartmentName
                    );
                }

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<ApprovalRecord> listHistory(User user) throws SQLException {
        String sql = """
                SELECT a.approval_id, a.document_id, d.document_code, d.title,
                       a.approver_id, u.full_name, a.decision, a.remarks,
                       oldd.department_name old_department,
                       newd.department_name new_department,
                       a.decision_at
                FROM dms_approval a
                JOIN dms_document d ON d.document_id=a.document_id
                JOIN dms_user u ON u.user_id=a.approver_id
                LEFT JOIN dms_department oldd ON oldd.department_id=a.previous_department_id
                LEFT JOIN dms_department newd ON newd.department_id=a.new_department_id
                WHERE (?=1 OR a.approver_id=?)
                ORDER BY a.decision_at DESC
                """;
        List<ApprovalRecord> list = new ArrayList<>();
        boolean admin = user.hasRole("CLERK") || user.hasRole("SYSTEM_ADMIN");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, admin ? 1 : 0);
            statement.setLong(2, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ApprovalRecord approval = new ApprovalRecord();
                    approval.setId(resultSet.getLong(1));
                    approval.setDocumentId(resultSet.getLong(2));
                    approval.setDocumentCode(resultSet.getString(3));
                    approval.setDocumentTitle(resultSet.getString(4));
                    approval.setApproverId(resultSet.getLong(5));
                    approval.setApproverName(resultSet.getString(6));
                    approval.setDecision(resultSet.getString(7));
                    approval.setRemarks(resultSet.getString(8));
                    approval.setPreviousDepartment(resultSet.getString(9));
                    approval.setFinalDepartment(resultSet.getString(10));
                    approval.setDecisionAt(resultSet.getTimestamp(11).toLocalDateTime());
                    list.add(approval);
                }
            }
        }
        return list;
    }

    private String departmentName(Connection connection, long departmentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT department_name FROM dms_department WHERE department_id=? AND status='ACTIVE'"
        )) {
            statement.setLong(1, departmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Selected destination department is invalid or inactive.");
                }
                return resultSet.getString(1);
            }
        }
    }

    private void notifyDepartmentUsers(
            Connection connection,
            long departmentId,
            long documentId,
            String documentCode,
            String departmentName
    ) throws SQLException {
        String sql = """
                INSERT INTO dms_notification(recipient_id,type,message,document_id,read_flag)
                SELECT DISTINCT u.user_id, 'DOCUMENT_ROUTED', ?, ?, 'N'
                FROM dms_user u
                JOIN dms_user_role ur ON ur.user_id=u.user_id
                JOIN dms_role r ON r.role_id=ur.role_id
                WHERE u.department_id=? AND u.status='ACTIVE' AND r.role_code='DEPARTMENT_USER'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, documentCode + " is now available in the " + departmentName + " folder.");
            statement.setLong(2, documentId);
            statement.setLong(3, departmentId);
            statement.executeUpdate();
        }
    }
}
