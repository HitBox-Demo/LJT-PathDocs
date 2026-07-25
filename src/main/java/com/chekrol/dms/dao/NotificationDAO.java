package com.chekrol.dms.dao;

import com.chekrol.dms.model.Notification;
import com.chekrol.dms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private static final String LIST_SQL = """
            SELECT notification_id, recipient_id, type, message,
                   document_id, read_flag, created_at
            FROM dms_notification
            WHERE recipient_id=?
            ORDER BY created_at DESC
            """;

    public List<Notification> listForUser(long userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(LIST_SQL)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapNotification(resultSet));
                }
            }
        }
        return notifications;
    }

    public void markAllRead(long userId) throws SQLException {
        String sql = """
                UPDATE dms_notification
                SET read_flag='Y', read_at=SYSTIMESTAMP
                WHERE recipient_id=? AND read_flag='N'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private Notification mapNotification(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();
        notification.setId(resultSet.getLong("notification_id"));
        notification.setRecipientId(resultSet.getLong("recipient_id"));
        notification.setType(resultSet.getString("type"));
        notification.setMessage(resultSet.getString("message"));

        long documentId = resultSet.getLong("document_id");
        notification.setDocumentId(resultSet.wasNull() ? null : documentId);
        notification.setRead("Y".equals(resultSet.getString("read_flag")));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        notification.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return notification;
    }
}
