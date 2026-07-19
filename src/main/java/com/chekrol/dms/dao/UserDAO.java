package com.chekrol.dms.dao;

import com.chekrol.dms.model.User;
import com.chekrol.dms.util.DatabaseConnection;
import com.chekrol.dms.util.PasswordUtil;

import java.sql.*;
import java.util.*;

public class UserDAO {
    private static final String BASE_SELECT = """
        SELECT u.user_id, u.username, u.password_hash, u.full_name, u.email, u.status,
               u.department_id, d.department_name, r.role_code
        FROM dms_user u
        LEFT JOIN dms_department d ON d.department_id = u.department_id
        LEFT JOIN dms_user_role ur ON ur.user_id = u.user_id
        LEFT JOIN dms_role r ON r.role_id = ur.role_id
        """;

    public User findByUsername(String username) throws SQLException {
        String sql = BASE_SELECT + " WHERE LOWER(u.username) = LOWER(?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return mapSingleUser(rs); }
        }
    }

    public List<User> listAll() throws SQLException {
        String sql = BASE_SELECT + " ORDER BY u.full_name, r.role_code";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return mapUsers(rs);
        }
    }

    public List<User> listBosses() throws SQLException {
        String sql = BASE_SELECT + " WHERE u.status='ACTIVE' AND r.role_code='BOSS' ORDER BY u.full_name";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return mapUsers(rs);
        }
    }

    public long createUser(User user, String temporaryPassword, String roleCode) throws SQLException {
        String insertUser = "INSERT INTO dms_user (username,password_hash,full_name,email,department_id,status) VALUES (?,?,?,?,?,'ACTIVE')";
        String findRole = "SELECT role_id FROM dms_role WHERE role_code=?";
        String insertRole = "INSERT INTO dms_user_role (user_id,role_id) VALUES (?,?)";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement ps = c.prepareStatement(insertUser, new String[]{"USER_ID"})) {
                    ps.setString(1, user.getUsername()); ps.setString(2, PasswordUtil.hash(temporaryPassword));
                    ps.setString(3, user.getFullName()); ps.setString(4, user.getEmail());
                    if (user.getDepartmentId() == null) ps.setNull(5, Types.NUMERIC); else ps.setLong(5, user.getDepartmentId());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Oracle did not return the new user ID.");
                        userId = keys.getLong(1);
                    }
                }
                long roleId;
                try (PreparedStatement ps = c.prepareStatement(findRole)) {
                    ps.setString(1, roleCode);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Role does not exist: " + roleCode);
                        roleId = rs.getLong(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(insertRole)) {
                    ps.setLong(1, userId); ps.setLong(2, roleId); ps.executeUpdate();
                }
                c.commit(); return userId;
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    private User mapSingleUser(ResultSet rs) throws SQLException {
        List<User> users = mapUsers(rs);
        return users.isEmpty() ? null : users.get(0);
    }

    private List<User> mapUsers(ResultSet rs) throws SQLException {
        Map<Long, User> map = new LinkedHashMap<>();
        while (rs.next()) {
            long id = rs.getLong("user_id");
            User user = map.computeIfAbsent(id, ignored -> {
                try {
                    User u = new User(); u.setId(id); u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash")); u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email")); u.setStatus(rs.getString("status"));
                    long dept = rs.getLong("department_id"); u.setDepartmentId(rs.wasNull() ? null : dept);
                    u.setDepartmentName(rs.getString("department_name")); return u;
                } catch (SQLException e) { throw new IllegalStateException(e); }
            });
            String role = rs.getString("role_code"); if (role != null) user.getRoles().add(role);
        }
        return new ArrayList<>(map.values());
    }
}
