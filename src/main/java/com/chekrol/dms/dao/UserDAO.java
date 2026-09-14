package com.chekrol.dms.dao;

import com.chekrol.dms.model.User;
import com.chekrol.dms.util.DatabaseConnection;
import com.chekrol.dms.util.PasswordUtil;
import com.chekrol.dms.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UserDAO {

    private static final Set<String> ALLOWED_INITIAL_ROLES = Set.of(
            "CLERK",
            "BOSS",
            "DEPARTMENT_USER"
    );

    private static final String BASE_SELECT = """
            SELECT u.user_id, u.username, u.password_hash, u.full_name,
                   u.email, u.status, u.department_id,
                   d.department_name, r.role_code
            FROM dms_user u
            LEFT JOIN dms_department d
                   ON d.department_id = u.department_id
            LEFT JOIN dms_user_role ur
                   ON ur.user_id = u.user_id
            LEFT JOIN dms_role r
                   ON r.role_id = ur.role_id
            """;

    public User findByUsername(String username) throws SQLException {
        if (!ValidationUtil.hasText(username)) {
            return null;
        }

        String sql = BASE_SELECT
                + " WHERE LOWER(u.username) = LOWER(?)"
                + " ORDER BY r.role_code";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapSingleUser(resultSet);
            }
        }
    }

    public List<User> listAll() throws SQLException {
        String sql = BASE_SELECT + " ORDER BY u.full_name, r.role_code";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapUsers(resultSet);
        }
    }

    public List<User> listBosses() throws SQLException {
        String sql = BASE_SELECT
                + " WHERE u.status='ACTIVE' AND r.role_code='BOSS'"
                + " ORDER BY u.full_name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapUsers(resultSet);
        }
    }

    public long createUser(
            User user,
            String temporaryPassword,
            String roleCode
    ) throws SQLException {
        validateNewUser(user, temporaryPassword, roleCode);
        String normalizedRole = roleCode.trim().toUpperCase(Locale.ROOT);

        String insertUser = """
                INSERT INTO dms_user(
                    username, password_hash, full_name, email,
                    department_id, status
                ) VALUES(?,?,?,?,?,'ACTIVE')
                """;
        String findRole = "SELECT role_id FROM dms_role WHERE role_code=?";
        String insertRole = """
                INSERT INTO dms_user_role(user_id, role_id)
                VALUES(?,?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureUsernameAndEmailAvailable(connection, user);
                validateDepartment(connection, user.getDepartmentId());

                long userId;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertUser,
                        new String[]{"USER_ID"}
                )) {
                    statement.setString(1, user.getUsername().trim());
                    statement.setString(2, PasswordUtil.hash(temporaryPassword));
                    statement.setString(3, user.getFullName().trim());
                    statement.setString(4, user.getEmail().trim());
                    if (user.getDepartmentId() == null) {
                        statement.setNull(5, Types.NUMERIC);
                    } else {
                        statement.setLong(5, user.getDepartmentId());
                    }
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Oracle did not return the new user ID."
                            );
                        }
                        userId = keys.getLong(1);
                    }
                }

                long roleId;
                try (PreparedStatement statement = connection.prepareStatement(findRole)) {
                    statement.setString(1, normalizedRole);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException(
                                    "Role does not exist: " + normalizedRole
                            );
                        }
                        roleId = resultSet.getLong(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(insertRole)) {
                    statement.setLong(1, userId);
                    statement.setLong(2, roleId);
                    statement.executeUpdate();
                }

                connection.commit();
                return userId;
            } catch (SQLException exception) {
                connection.rollback();
                if (isUniqueConstraintViolation(exception)) {
                    throw new SQLException(
                            "Username or email already exists.",
                            exception
                    );
                }
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void validateNewUser(
            User user,
            String temporaryPassword,
            String roleCode
    ) {
        if (user == null
                || !ValidationUtil.hasText(user.getUsername())
                || !ValidationUtil.hasText(user.getFullName())
                || !ValidationUtil.hasText(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Username, full name and email are required."
            );
        }
        if (temporaryPassword == null || temporaryPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Temporary password must contain at least 8 characters."
            );
        }

        String normalizedRole = roleCode == null
                ? ""
                : roleCode.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_INITIAL_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("Invalid initial role.");
        }
        if ("DEPARTMENT_USER".equals(normalizedRole)
                && user.getDepartmentId() == null) {
            throw new IllegalArgumentException(
                    "Department users must be assigned to a department."
            );
        }
    }

    private void ensureUsernameAndEmailAvailable(
            Connection connection,
            User user
    ) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM dms_user
                WHERE LOWER(username)=LOWER(?)
                   OR LOWER(email)=LOWER(?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername().trim());
            statement.setString(2, user.getEmail().trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    throw new SQLException("Username or email already exists.");
                }
            }
        }
    }

    private void validateDepartment(
            Connection connection,
            Long departmentId
    ) throws SQLException {
        if (departmentId == null) {
            return;
        }

        String sql = """
                SELECT 1
                FROM dms_department
                WHERE department_id=? AND status='ACTIVE'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, departmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException(
                            "Selected department is invalid or inactive."
                    );
                }
            }
        }
    }

    private boolean isUniqueConstraintViolation(SQLException exception) {
        SQLException current = exception;
        while (current != null) {
            if (current.getErrorCode() == 1
                    || "23000".equals(current.getSQLState())) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }

    private User mapSingleUser(ResultSet resultSet) throws SQLException {
        List<User> users = mapUsers(resultSet);
        return users.isEmpty() ? null : users.get(0);
    }

    private List<User> mapUsers(ResultSet resultSet) throws SQLException {
        Map<Long, User> users = new LinkedHashMap<>();

        while (resultSet.next()) {
            long id = resultSet.getLong("user_id");
            User user = users.get(id);

            if (user == null) {
                user = new User();
                user.setId(id);
                user.setUsername(resultSet.getString("username"));
                user.setPasswordHash(resultSet.getString("password_hash"));
                user.setFullName(resultSet.getString("full_name"));
                user.setEmail(resultSet.getString("email"));
                user.setStatus(resultSet.getString("status"));

                long departmentId = resultSet.getLong("department_id");
                user.setDepartmentId(
                        resultSet.wasNull() ? null : departmentId
                );
                user.setDepartmentName(
                        resultSet.getString("department_name")
                );
                users.put(id, user);
            }

            String role = resultSet.getString("role_code");
            if (role != null) {
                user.addRole(role);
            }
        }

        return new ArrayList<>(users.values());
    }
}
