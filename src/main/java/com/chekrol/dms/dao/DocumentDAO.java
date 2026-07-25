package com.chekrol.dms.dao;

import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DocumentDAO {
    private static final String SELECT_DOCUMENTS = """
            SELECT doc.document_id, doc.document_code, doc.title,
                   doc.reference_no, doc.sender, doc.date_received,
                   doc.category, doc.priority, doc.description,
                   doc.destination_department_id, dept.department_name,
                   doc.boss_id, boss.full_name AS boss_name,
                   doc.status, doc.created_by, creator.full_name AS creator_name,
                   doc.created_at, doc.submitted_at, doc.due_date,
                   doc.approved_at, doc.rejection_reason,
                   pf.original_name AS primary_file_name,
                   pf.storage_path AS primary_file_path
            FROM dms_document doc
            LEFT JOIN dms_department dept
                   ON dept.department_id=doc.destination_department_id
            LEFT JOIN dms_user boss ON boss.user_id=doc.boss_id
            LEFT JOIN dms_user creator ON creator.user_id=doc.created_by
            LEFT JOIN dms_document_file pf
                   ON pf.document_id=doc.document_id AND pf.primary_flag='Y'
            """;

    public List<DocumentRecord> listForUser(User user, String view, String query)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_DOCUMENTS).append(" WHERE 1=1 ");
        List<Object> parameters = new ArrayList<>();
        appendAccessCondition(sql, parameters, user);
        appendViewCondition(sql, parameters, user, view);
        appendSearchCondition(sql, parameters, query);
        sql.append(" ORDER BY doc.created_at DESC");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapDocuments(resultSet);
            }
        }
    }

    public DocumentRecord findAuthorized(User user, long documentId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_DOCUMENTS)
                .append(" WHERE doc.document_id=? ");
        List<Object> parameters = new ArrayList<>();
        parameters.add(documentId);
        appendAccessCondition(sql, parameters, user);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<DocumentRecord> documents = mapDocuments(resultSet);
                return documents.isEmpty() ? null : documents.get(0);
            }
        }
    }

    public List<DocumentFile> listFiles(long documentId) throws SQLException {
        String sql = """
                SELECT file_id, document_id, original_name, storage_name,
                       storage_path, mime_type, file_size, primary_flag
                FROM dms_document_file
                WHERE document_id=?
                ORDER BY primary_flag DESC, file_id
                """;
        List<DocumentFile> files = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, documentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    files.add(mapFile(resultSet));
                }
            }
        }
        return files;
    }

    public DocumentFile findFileAuthorized(User user, long fileId) throws SQLException {
        String sql = """
                SELECT f.file_id, f.document_id, f.original_name, f.storage_name,
                       f.storage_path, f.mime_type, f.file_size, f.primary_flag,
                       doc.created_by, doc.boss_id, doc.destination_department_id,
                       doc.status
                FROM dms_document_file f
                JOIN dms_document doc ON doc.document_id=f.document_id
                WHERE f.file_id=?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, fileId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || !canDownload(user, resultSet)) {
                    return null;
                }
                return mapFile(resultSet);
            }
        }
    }

    public long create(
            DocumentRecord document,
            List<DocumentFile> files,
            boolean submit
    ) throws SQLException {
        String insertDocumentSql = """
                INSERT INTO dms_document(
                    document_code, title, reference_no, sender, date_received,
                    category, priority, confidential_flag, description,
                    destination_department_id, boss_id, status, created_by,
                    submitted_at, due_date
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long documentId = insertDocument(connection, insertDocumentSql, document, submit);
                for (DocumentFile file : files) {
                    insertFile(connection, documentId, file);
                }
                insertHistory(
                        connection,
                        documentId,
                        document.getCreatedById(),
                        submit ? "SUBMITTED" : "CREATED_DRAFT",
                        null,
                        submit ? "PENDING_APPROVAL" : "DRAFT",
                        null
                );
                if (submit) {
                    insertNotification(
                            connection,
                            document.getBossId(),
                            "PENDING_APPROVAL",
                            document.getDocumentCode() + " is waiting for your approval.",
                            documentId
                    );
                }
                connection.commit();
                return documentId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void update(
            User user,
            DocumentRecord document,
            List<DocumentFile> newFiles,
            Set<Long> removeFileIds,
            boolean replacePrimary,
            String action
    ) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                EditableState state = lockEditableDocument(connection, document.getId(), user.getId());
                UpdateState updateState = determineUpdateState(state, document, action);
                updateDocumentRow(connection, document, user.getId(), updateState);

                if (replacePrimary) {
                    deletePrimaryMetadata(connection, document.getId());
                }
                deleteAttachmentMetadata(connection, document.getId(), removeFileIds);
                for (DocumentFile file : newFiles) {
                    insertFile(connection, document.getId(), file);
                }

                insertHistory(
                        connection,
                        document.getId(),
                        user.getId(),
                        "resubmit".equals(action) ? "RESUBMITTED" : "DOCUMENT_CORRECTED",
                        state.status(),
                        updateState.status(),
                        "Document metadata, routing or files were updated."
                );

                if ("resubmit".equals(action)) {
                    insertNotification(
                            connection,
                            document.getBossId(),
                            "PENDING_APPROVAL",
                            document.getDocumentCode()
                                    + " was corrected and resubmitted for your approval.",
                            document.getId()
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

    public Map<String, Long> dashboardStats(User user) throws SQLException {
        Map<String, Long> statistics = new LinkedHashMap<>();
        for (String key : List.of("TOTAL", "PENDING", "RETURNED", "ROUTED", "OVERDUE")) {
            statistics.put(key, 0L);
        }

        List<DocumentRecord> documents = listForUser(user, "repository", "");
        statistics.put("TOTAL", (long) documents.size());
        statistics.put(
                "PENDING",
                documents.stream().filter(document ->
                        "PENDING_APPROVAL".equals(document.getStatus())).count()
        );
        statistics.put(
                "RETURNED",
                documents.stream().filter(document ->
                        "RETURNED_FOR_CORRECTION".equals(document.getStatus())).count()
        );
        statistics.put(
                "ROUTED",
                documents.stream().filter(document ->
                        "ROUTED".equals(document.getStatus())).count()
        );
        statistics.put(
                "OVERDUE",
                documents.stream().filter(document ->
                        "PENDING_APPROVAL".equals(document.getStatus())
                                && document.getDueDate() != null
                                && document.getDueDate().isBefore(LocalDate.now())
                ).count()
        );
        return statistics;
    }

    public void recall(User user, long documentId) throws SQLException {
        String lockSql = """
                SELECT boss_id, document_code
                FROM dms_document
                WHERE document_id=? AND created_by=? AND status='PENDING_APPROVAL'
                FOR UPDATE
                """;
        String updateSql = """
                UPDATE dms_document
                SET status='RECALLED', submitted_at=NULL, due_date=NULL,
                    updated_at=SYSTIMESTAMP
                WHERE document_id=? AND created_by=? AND status='PENDING_APPROVAL'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                RecallTarget target = lockRecallTarget(connection, lockSql, documentId, user.getId());
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setLong(1, documentId);
                    statement.setLong(2, user.getId());
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Document cannot be recalled.");
                    }
                }

                insertHistory(
                        connection,
                        documentId,
                        user.getId(),
                        "RECALLED",
                        "PENDING_APPROVAL",
                        "RECALLED",
                        null
                );
                insertNotification(
                        connection,
                        target.bossId(),
                        "RECALLED",
                        target.documentCode() + " was recalled by the clerk.",
                        documentId
                );
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    static void insertNotification(
            Connection connection,
            long recipientId,
            String type,
            String message,
            long documentId
    ) throws SQLException {
        String sql = """
                INSERT INTO dms_notification(
                    recipient_id, type, message, document_id, read_flag
                ) VALUES(?,?,?,?, 'N')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, recipientId);
            statement.setString(2, type);
            statement.setString(3, message);
            statement.setLong(4, documentId);
            statement.executeUpdate();
        }
    }

    private void appendAccessCondition(
            StringBuilder sql,
            List<Object> parameters,
            User user
    ) {
        if (user.hasRole("SYSTEM_ADMIN") || user.hasRole("CLERK")) {
            return;
        }
        if (user.hasRole("BOSS")) {
            sql.append(" AND doc.boss_id=?");
            parameters.add(user.getId());
            return;
        }
        sql.append(" AND doc.status='ROUTED' AND doc.destination_department_id=?");
        parameters.add(user.getDepartmentId() == null ? -1L : user.getDepartmentId());
    }

    private void appendViewCondition(
            StringBuilder sql,
            List<Object> parameters,
            User user,
            String view
    ) {
        switch (view == null ? "repository" : view) {
            case "draft" -> {
                sql.append(" AND doc.status='DRAFT' AND doc.created_by=?");
                parameters.add(user.getId());
            }
            case "pending" -> sql.append(" AND doc.status='PENDING_APPROVAL'");
            case "returned" -> {
                sql.append(" AND doc.status='RETURNED_FOR_CORRECTION' AND doc.created_by=?");
                parameters.add(user.getId());
            }
            case "department" -> sql.append(" AND doc.status='ROUTED'");
            default -> {
                // Repository and search use only the role access condition.
            }
        }
    }

    private void appendSearchCondition(
            StringBuilder sql,
            List<Object> parameters,
            String query
    ) {
        if (query == null || query.isBlank()) {
            return;
        }
        sql.append("""
                 AND (
                    LOWER(doc.document_code) LIKE ?
                    OR LOWER(doc.title) LIKE ?
                    OR LOWER(doc.reference_no) LIKE ?
                    OR LOWER(doc.sender) LIKE ?
                 )
                """);
        String search = "%" + query.toLowerCase(Locale.ROOT) + "%";
        parameters.add(search);
        parameters.add(search);
        parameters.add(search);
        parameters.add(search);
    }

    private boolean canDownload(User user, ResultSet resultSet) throws SQLException {
        long creatorId = resultSet.getLong("created_by");
        long bossId = resultSet.getLong("boss_id");
        long departmentId = resultSet.getLong("destination_department_id");
        String status = resultSet.getString("status");

        return user.hasRole("CLERK")
                || user.hasRole("SYSTEM_ADMIN")
                || creatorId == user.getId()
                || bossId == user.getId()
                || (user.hasRole("DEPARTMENT_USER")
                    && user.getDepartmentId() != null
                    && departmentId == user.getDepartmentId()
                    && "ROUTED".equals(status));
    }

    private long insertDocument(
            Connection connection,
            String sql,
            DocumentRecord document,
            boolean submit
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                new String[]{"DOCUMENT_ID"}
        )) {
            int index = 1;
            statement.setString(index++, document.getDocumentCode());
            statement.setString(index++, document.getTitle());
            statement.setString(index++, document.getReferenceNo());
            statement.setString(index++, document.getSender());
            statement.setDate(index++, Date.valueOf(document.getDateReceived()));
            statement.setString(index++, document.getCategory());
            statement.setString(index++, document.getPriority());
            statement.setString(index++, "N");
            statement.setString(index++, document.getDescription());
            statement.setLong(index++, document.getDestinationDepartmentId());
            statement.setLong(index++, document.getBossId());
            statement.setString(index++, submit ? "PENDING_APPROVAL" : "DRAFT");
            statement.setLong(index++, document.getCreatedById());

            if (submit) {
                statement.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
                statement.setDate(index, Date.valueOf(document.getDueDate()));
            } else {
                statement.setNull(index++, Types.TIMESTAMP);
                statement.setNull(index, Types.DATE);
            }

            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Oracle did not return the new document ID.");
                }
                return keys.getLong(1);
            }
        }
    }

    private void insertFile(Connection connection, long documentId, DocumentFile file)
            throws SQLException {
        String sql = """
                INSERT INTO dms_document_file(
                    document_id, original_name, storage_name, storage_path,
                    mime_type, file_size, primary_flag
                ) VALUES(?,?,?,?,?,?,?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, documentId);
            statement.setString(2, file.getOriginalName());
            statement.setString(3, file.getStorageName());
            statement.setString(4, file.getStoragePath());
            statement.setString(5, file.getMimeType());
            statement.setLong(6, file.getFileSize());
            statement.setString(7, file.isPrimaryFile() ? "Y" : "N");
            statement.executeUpdate();
        }
    }

    private void insertHistory(
            Connection connection,
            long documentId,
            long userId,
            String action,
            String oldValue,
            String newValue,
            String remarks
    ) throws SQLException {
        String sql = """
                INSERT INTO dms_document_history(
                    document_id, user_id, action, old_value, new_value, remarks
                ) VALUES(?,?,?,?,?,?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, documentId);
            statement.setLong(2, userId);
            statement.setString(3, action);
            statement.setString(4, oldValue);
            statement.setString(5, newValue);
            statement.setString(6, remarks);
            statement.executeUpdate();
        }
    }

    private EditableState lockEditableDocument(
            Connection connection,
            long documentId,
            long creatorId
    ) throws SQLException {
        String sql = """
                SELECT status, rejection_reason
                FROM dms_document
                WHERE document_id=? AND created_by=?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, documentId);
            statement.setLong(2, creatorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Document not found or edit access denied.");
                }
                String status = resultSet.getString("status");
                if (!Set.of("RETURNED_FOR_CORRECTION", "DRAFT", "RECALLED").contains(status)) {
                    throw new SQLException("Only returned, recalled or draft documents can be edited.");
                }
                return new EditableState(status, resultSet.getString("rejection_reason"));
            }
        }
    }

    private UpdateState determineUpdateState(
            EditableState previous,
            DocumentRecord document,
            String action
    ) throws SQLException {
        return switch (action) {
            case "resubmit" -> new UpdateState(
                    "PENDING_APPROVAL",
                    Timestamp.valueOf(LocalDateTime.now()),
                    Date.valueOf(LocalDate.now().plusDays(
                            "URGENT".equals(document.getPriority()) ? 2 : 5
                    )),
                    null
            );
            case "draft" -> new UpdateState("DRAFT", null, null, previous.rejectionReason());
            case "save" -> new UpdateState(
                    "RECALLED".equals(previous.status()) ? "DRAFT" : previous.status(),
                    null,
                    null,
                    previous.rejectionReason()
            );
            default -> throw new SQLException("Unsupported document edit action.");
        };
    }

    private void updateDocumentRow(
            Connection connection,
            DocumentRecord document,
            long creatorId,
            UpdateState state
    ) throws SQLException {
        String sql = """
                UPDATE dms_document
                SET title=?, reference_no=?, sender=?, date_received=?,
                    category=?, priority=?, confidential_flag='N', description=?,
                    destination_department_id=?, boss_id=?, status=?,
                    submitted_at=?, due_date=?, approved_at=NULL,
                    rejection_reason=?, updated_at=SYSTIMESTAMP
                WHERE document_id=? AND created_by=?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, document.getTitle());
            statement.setString(index++, document.getReferenceNo());
            statement.setString(index++, document.getSender());
            statement.setDate(index++, Date.valueOf(document.getDateReceived()));
            statement.setString(index++, document.getCategory());
            statement.setString(index++, document.getPriority());
            statement.setString(index++, document.getDescription());
            statement.setLong(index++, document.getDestinationDepartmentId());
            statement.setLong(index++, document.getBossId());
            statement.setString(index++, state.status());

            if (state.submittedAt() == null) {
                statement.setNull(index++, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(index++, state.submittedAt());
            }
            if (state.dueDate() == null) {
                statement.setNull(index++, Types.DATE);
            } else {
                statement.setDate(index++, state.dueDate());
            }

            statement.setString(index++, state.rejectionReason());
            statement.setLong(index++, document.getId());
            statement.setLong(index, creatorId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Document update failed.");
            }
        }
    }

    private void deletePrimaryMetadata(Connection connection, long documentId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM dms_document_file WHERE document_id=? AND primary_flag='Y'"
        )) {
            statement.setLong(1, documentId);
            statement.executeUpdate();
        }
    }

    private void deleteAttachmentMetadata(
            Connection connection,
            long documentId,
            Set<Long> fileIds
    ) throws SQLException {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        String sql = """
                DELETE FROM dms_document_file
                WHERE file_id=? AND document_id=? AND primary_flag='N'
                """;
        for (Long fileId : fileIds) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, fileId);
                statement.setLong(2, documentId);
                statement.executeUpdate();
            }
        }
    }

    private RecallTarget lockRecallTarget(
            Connection connection,
            String sql,
            long documentId,
            long creatorId
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, documentId);
            statement.setLong(2, creatorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Document cannot be recalled.");
                }
                return new RecallTarget(
                        resultSet.getLong("boss_id"),
                        resultSet.getString("document_code")
                );
            }
        }
    }

    private void bind(PreparedStatement statement, List<Object> parameters)
            throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            if (value instanceof Long number) {
                statement.setLong(index + 1, number);
            } else {
                statement.setString(index + 1, String.valueOf(value));
            }
        }
    }

    private List<DocumentRecord> mapDocuments(ResultSet resultSet) throws SQLException {
        List<DocumentRecord> documents = new ArrayList<>();
        while (resultSet.next()) {
            DocumentRecord document = new DocumentRecord();
            document.setId(resultSet.getLong("document_id"));
            document.setDocumentCode(resultSet.getString("document_code"));
            document.setTitle(resultSet.getString("title"));
            document.setReferenceNo(resultSet.getString("reference_no"));
            document.setSender(resultSet.getString("sender"));

            Date dateReceived = resultSet.getDate("date_received");
            document.setDateReceived(dateReceived == null ? null : dateReceived.toLocalDate());
            document.setCategory(resultSet.getString("category"));
            document.setPriority(resultSet.getString("priority"));
            document.setDescription(resultSet.getString("description"));

            long departmentId = resultSet.getLong("destination_department_id");
            document.setDestinationDepartmentId(resultSet.wasNull() ? null : departmentId);
            document.setDestinationDepartmentName(resultSet.getString("department_name"));

            long bossId = resultSet.getLong("boss_id");
            document.setBossId(resultSet.wasNull() ? null : bossId);
            document.setBossName(resultSet.getString("boss_name"));
            document.setStatus(resultSet.getString("status"));

            long creatorId = resultSet.getLong("created_by");
            document.setCreatedById(resultSet.wasNull() ? null : creatorId);
            document.setCreatedByName(resultSet.getString("creator_name"));

            Timestamp timestamp = resultSet.getTimestamp("created_at");
            document.setCreatedAt(timestamp == null ? null : timestamp.toLocalDateTime());
            timestamp = resultSet.getTimestamp("submitted_at");
            document.setSubmittedAt(timestamp == null ? null : timestamp.toLocalDateTime());

            Date dueDate = resultSet.getDate("due_date");
            document.setDueDate(dueDate == null ? null : dueDate.toLocalDate());
            timestamp = resultSet.getTimestamp("approved_at");
            document.setApprovedAt(timestamp == null ? null : timestamp.toLocalDateTime());

            document.setRejectionReason(resultSet.getString("rejection_reason"));
            document.setPrimaryFileName(resultSet.getString("primary_file_name"));
            document.setPrimaryFilePath(resultSet.getString("primary_file_path"));

            if (document.getSubmittedAt() != null
                    && "PENDING_APPROVAL".equals(document.getStatus())) {
                document.setDaysPending((int) ChronoUnit.DAYS.between(
                        document.getSubmittedAt().toLocalDate(),
                        LocalDate.now()
                ));
            }
            documents.add(document);
        }
        return documents;
    }

    private DocumentFile mapFile(ResultSet resultSet) throws SQLException {
        DocumentFile file = new DocumentFile();
        file.setId(resultSet.getLong("file_id"));
        file.setDocumentId(resultSet.getLong("document_id"));
        file.setOriginalName(resultSet.getString("original_name"));
        file.setStorageName(resultSet.getString("storage_name"));
        file.setStoragePath(resultSet.getString("storage_path"));
        file.setMimeType(resultSet.getString("mime_type"));
        file.setFileSize(resultSet.getLong("file_size"));
        file.setPrimaryFile("Y".equals(resultSet.getString("primary_flag")));
        return file;
    }

    private record EditableState(String status, String rejectionReason) {
    }

    private record UpdateState(
            String status,
            Timestamp submittedAt,
            Date dueDate,
            String rejectionReason
    ) {
    }

    private record RecallTarget(long bossId, String documentCode) {
    }
}
