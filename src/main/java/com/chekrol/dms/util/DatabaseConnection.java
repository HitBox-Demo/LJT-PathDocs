package com.chekrol.dms.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static volatile DataSource dataSource;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        DataSource ds = resolveJndiDataSource();
        if (ds != null) return ds.getConnection();

        String url = AppConfig.value("DMS_DB_URL", "");
        String username = AppConfig.value("DMS_DB_USERNAME", "");
        String password = AppConfig.value("DMS_DB_PASSWORD", "");
        if (url.isBlank() || username.isBlank()) {
            throw new SQLException("Oracle configuration is missing. Set DMS_DB_URL, DMS_DB_USERNAME and DMS_DB_PASSWORD, or configure JNDI jdbc/DocumentManagementDB.");
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static String configurationSource() {
        if (resolveJndiDataSource() != null) return "Tomcat JNDI: java:comp/env/jdbc/DocumentManagementDB";
        if (!AppConfig.value("DMS_DB_URL", "").isBlank()) return "Environment variables / Java system properties";
        return "Not configured";
    }

    private static DataSource resolveJndiDataSource() {
        if (dataSource != null) return dataSource;
        synchronized (DatabaseConnection.class) {
            if (dataSource != null) return dataSource;
            try {
                Object value = new InitialContext().lookup("java:comp/env/jdbc/DocumentManagementDB");
                if (value instanceof DataSource) dataSource = (DataSource) value;
            } catch (NamingException ignored) {
                // Environment variable mode is supported when JNDI is not configured.
            }
            return dataSource;
        }
    }
}
