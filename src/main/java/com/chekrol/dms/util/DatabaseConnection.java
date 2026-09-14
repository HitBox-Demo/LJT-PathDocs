package com.chekrol.dms.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final String JNDI_NAME =
            "java:comp/env/jdbc/DocumentManagementDB";

    private static final String ORACLE_DRIVER_CLASS =
            "oracle.jdbc.OracleDriver";

    private static volatile DataSource dataSource;
    private static volatile boolean oracleDriverLoaded;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (hasDirectConfiguration()) {
            return directConnection();
        }

        DataSource jndiDataSource = resolveJndiDataSource();

        if (jndiDataSource != null) {
            return jndiDataSource.getConnection();
        }

        throw new SQLException(
                "Oracle configuration is missing. Set DMS_DB_URL, "
                        + "DMS_DB_USERNAME and DMS_DB_PASSWORD, or configure "
                        + "JNDI jdbc/DocumentManagementDB."
        );
    }

    public static boolean isConfigured() {
        return hasDirectConfiguration()
                || resolveJndiDataSource() != null;
    }

    public static String configurationSource() {
        if (hasDirectConfiguration()) {
            return "Environment variables / Java system properties";
        }

        if (resolveJndiDataSource() != null) {
            return "Tomcat JNDI: " + JNDI_NAME;
        }

        return "Not configured";
    }

    private static boolean hasDirectConfiguration() {
        return !AppConfig.value("DMS_DB_URL", "").isBlank()
                && !AppConfig.value(
                        "DMS_DB_USERNAME",
                        ""
                ).isBlank();
    }

    private static Connection directConnection() throws SQLException {
        ensureOracleDriverLoaded();

        String url = AppConfig.value("DMS_DB_URL", "");
        String username = AppConfig.value(
                "DMS_DB_USERNAME",
                ""
        );
        String password = AppConfig.value(
                "DMS_DB_PASSWORD",
                ""
        );

        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty(
                "oracle.net.CONNECT_TIMEOUT",
                AppConfig.value(
                        "DMS_DB_CONNECT_TIMEOUT_MS",
                        "10000"
                )
        );
        properties.setProperty(
                "oracle.jdbc.ReadTimeout",
                AppConfig.value(
                        "DMS_DB_READ_TIMEOUT_MS",
                        "30000"
                )
        );
        properties.setProperty(
                "oracle.net.keepAlive",
                "true"
        );

        return DriverManager.getConnection(url, properties);
    }

    private static void ensureOracleDriverLoaded()
            throws SQLException {

        if (oracleDriverLoaded) {
            return;
        }

        synchronized (DatabaseConnection.class) {
            if (oracleDriverLoaded) {
                return;
            }

            try {
                Class.forName(
                        ORACLE_DRIVER_CLASS,
                        true,
                        DatabaseConnection.class.getClassLoader()
                );

                oracleDriverLoaded = true;

            } catch (ClassNotFoundException exception) {
                throw new SQLException(
                        "Oracle JDBC driver is missing. Ensure ojdbc11.jar "
                                + "exists in WEB-INF/lib or Tomcat lib.",
                        exception
                );
            }
        }
    }

    private static DataSource resolveJndiDataSource() {
        if (dataSource != null) {
            return dataSource;
        }

        synchronized (DatabaseConnection.class) {
            if (dataSource != null) {
                return dataSource;
            }

            try {
                Object value = new InitialContext().lookup(
                        JNDI_NAME
                );

                if (value instanceof DataSource resolved) {
                    dataSource = resolved;
                }

            } catch (NamingException ignored) {
                // Direct DMS_DB_* configuration works without JNDI.
            }

            return dataSource;
        }
    }
}
