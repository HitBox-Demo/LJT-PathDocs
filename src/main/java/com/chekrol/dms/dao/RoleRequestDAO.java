package com.chekrol.dms.dao;

import com.chekrol.dms.model.RoleRequest;
import com.chekrol.dms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RoleRequestDAO {
    private static final String LIST_PENDING_SQL = """
            SELECT rr.request_id, rr.user_id, u.full_name,
                   rr.current_role_code, rr.requested_role_code,
                   rr.requested_by, rb.full_name,
                   rr.status, rr.requested_at, rr.remarks
            FROM dms_role_change_request rr
            JOIN dms_user u ON u.user_id=rr.user_id
            JOIN dms_user rb ON rb.user_id=rr.requested_by
            WHERE rr.status='PENDING'
            ORDER BY rr.requested_at
            """;

    public List<RoleRequest> listPending() throws SQLException {
        List<RoleRequest> requests = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(LIST_PENDING_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                requests.add(mapRequest(resultSet));
            }
        }
        return requests;
    }

    public void create(
            long userId,
            String currentRole,
            String requestedRole,
            long requestedBy,
            String remarks
    ) throws SQLException {
        String sql = """
                INSERT INTO dms_role_change_request(
                    user_id, current_role_code, requested_role_code,
                    requested_by, status, remarks
                ) VALUES(?,?,?,?,'PENDING',?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, currentRole);
            statement.setString(3, requestedRole);
            statement.setLong(4, requestedBy);
            statement.setString(5, remarks);
            statement.executeUpdate();
        }
    }

    public void decide(
            long requestId,
            long bossId,
            String decision,
            String remarks
    ) throws SQLException {
        if (!"APPROVE".equals(decision) && !"REJECT".equals(decision)) {
            throw new SQLException("Unsupported role decision.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PendingRoleChange roleChange = lockPendingRequest(connection, requestId);
                if ("APPROVE".equals(decision)) {
                    applyRoleChange(connection, roleChange);
                }
                updateRequestDecision(connection, requestId, bossId, decision, remarks);
                notifyRequester(connection, roleChange.requestedBy(), requestId, decision);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private PendingRoleChange lockPendingRequest(Connection connection, long requestId)
            throws SQLException {
        String sql = """
                SELECT user_id, current_role_code, requested_role_code, requested_by
                FROM dms_role_change_request
                WHERE request_id=? AND status='PENDING'
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Role request is no longer pending.");
                }
                return new PendingRoleChange(
                        resultSet.getLong("user_id"),
                        resultSet.getString("current_role_code"),
                        resultSet.getString("requested_role_code"),
                        resultSet.getLong("requested_by")
                );
            }
        }
    }

    private void applyRoleChange(Connection connection, PendingRoleChange roleChange)
            throws SQLException {
        if (roleChange.currentRole() != null && !roleChange.currentRole().isBlank()) {
            String deleteSql = """
                    DELETE FROM dms_user_role
                    WHERE user_id=?
                      AND role_id=(SELECT role_id FROM dms_role WHERE role_code=?)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
                statement.setLong(1, roleChange.userId());
                statement.setString(2, roleChange.currentRole());
                statement.executeUpdate();
            }
        }

        long roleId = findRoleId(connection, roleChange.requestedRole());
        String insertSql = """
                INSERT INTO dms_user_role(user_id, role_id)
                SELECT ?, ? FROM dual
                WHERE NOT EXISTS(
                    SELECT 1 FROM dms_user_role WHERE user_id=? AND role_id=?
                )
                """;
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setLong(1, roleChange.userId());
            statement.setLong(2, roleId);
            statement.setLong(3, roleChange.userId());
            statement.setLong(4, roleId);
            statement.executeUpdate();
        }
    }

    private long findRoleId(Connection connection, String roleCode) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT role_id FROM dms_role WHERE role_code=?"
        )) {
            statement.setString(1, roleCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Requested role does not exist.");
                }
                return resultSet.getLong(1);
            }
        }
    }

    private void updateRequestDecision(
            Connection connection,
            long requestId,
            long bossId,
            String decision,
            String remarks
    ) throws SQLException {
        String sql = """
                UPDATE dms_role_change_request
                SET status=?, approved_by=?, remarks=?, decided_at=SYSTIMESTAMP
                WHERE request_id=?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "APPROVE".equals(decision) ? "APPROVED" : "REJECTED");
            statement.setLong(2, bossId);
            statement.setString(3, remarks);
            statement.setLong(4, requestId);
            statement.executeUpdate();
        }
    }

    private void notifyRequester(
            Connection connection,
            long requesterId,
            long requestId,
            String decision
    ) throws SQLException {
        String result = "APPROVE".equals(decision) ? "approved" : "rejected";
        String sql = """
                INSERT INTO dms_notification(recipient_id, type, message, read_flag)
                VALUES(?, 'ROLE_REQUEST', ?, 'N')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, requesterId);
            statement.setString(2, "Role request #" + requestId + " was " + result + ".");
            statement.executeUpdate();
        }
    }

    private RoleRequest mapRequest(ResultSet resultSet) throws SQLException {
        RoleRequest request = new RoleRequest();
        request.setId(resultSet.getLong("request_id"));
        request.setUserId(resultSet.getLong("user_id"));
        request.setUserName(resultSet.getString("full_name"));
        request.setCurrentRole(resultSet.getString("current_role_code"));
        request.setRequestedRole(resultSet.getString("requested_role_code"));
        request.setRequestedById(resultSet.getLong("requested_by"));
        request.setRequestedByName(resultSet.getString(7));
        request.setStatus(resultSet.getString("status"));

        Timestamp requestedAt = resultSet.getTimestamp("requested_at");
        request.setRequestedAt(requestedAt == null ? null : requestedAt.toLocalDateTime());
        request.setRemarks(resultSet.getString("remarks"));
        return request;
    }

    private record PendingRoleChange(
            long userId,
            String currentRole,
            String requestedRole,
            long requestedBy
    ) {
    }
}
