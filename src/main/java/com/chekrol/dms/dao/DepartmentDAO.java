package com.chekrol.dms.dao;

import com.chekrol.dms.model.Department;
import com.chekrol.dms.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {
    public List<Department> listActive() throws SQLException {
        String sql = "SELECT department_id,department_code,department_name,status FROM dms_department WHERE status='ACTIVE' ORDER BY department_name";
        List<Department> result = new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while (rs.next()) result.add(new Department(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)));
        }
        return result;
    }

    public Department findById(Connection c, long id) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement("SELECT department_id,department_code,department_name,status FROM dms_department WHERE department_id=?")) {
            ps.setLong(1,id);
            try (ResultSet rs=ps.executeQuery()) { return rs.next()?new Department(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4)):null; }
        }
    }
}
