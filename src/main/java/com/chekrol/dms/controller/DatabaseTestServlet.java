package com.chekrol.dms.controller;

import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/setup/database-test")
public class DatabaseTestServlet extends HttpServlet {

    private static final String DATABASE_INFO_SQL = """
            SELECT SYS_CONTEXT('USERENV','DB_NAME') AS db_name,
                   SYS_CONTEXT('USERENV','SERVICE_NAME') AS service_name,
                   SYS_CONTEXT('USERENV','SERVER_HOST') AS server_host,
                   SYS_CONTEXT('USERENV','CURRENT_USER') AS current_user,
                   SYS_CONTEXT('USERENV','CON_NAME') AS container_name
            FROM dual
            """;

    private static final String TABLE_COUNT_SQL = """
            SELECT COUNT(*)
            FROM user_tables
            WHERE table_name IN (
                'DMS_DEPARTMENT', 'DMS_ROLE', 'DMS_USER', 'DMS_USER_ROLE',
                'DMS_DOCUMENT', 'DMS_DOCUMENT_FILE', 'DMS_APPROVAL',
                'DMS_DOCUMENT_HISTORY', 'DMS_NOTIFICATION',
                'DMS_ROLE_CHANGE_REQUEST'
            )
            """;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setAttribute("pageTitle", "Database Test");
        request.setAttribute("demoMode", AppConfig.isDemoMode());
        request.setAttribute(
                "databaseConfigured",
                DatabaseConnection.isConfigured()
        );
        request.setAttribute(
                "configSource",
                DatabaseConnection.configurationSource()
        );
        request.setAttribute(
                "storageRoot",
                AppConfig.storageRoot().toString()
        );

        if (DatabaseConnection.isConfigured()) {
            testConnection(request);
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/setup/database-test.jsp"
        ).forward(request, response);
    }

    private void testConnection(HttpServletRequest request) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    DATABASE_INFO_SQL
            ); ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    request.setAttribute("dbName", resultSet.getString("db_name"));
                    request.setAttribute(
                            "serviceName",
                            resultSet.getString("service_name")
                    );
                    request.setAttribute(
                            "serverHost",
                            resultSet.getString("server_host")
                    );
                    request.setAttribute(
                            "dbUser",
                            resultSet.getString("current_user")
                    );
                    request.setAttribute(
                            "containerName",
                            resultSet.getString("container_name")
                    );
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    TABLE_COUNT_SQL
            ); ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    request.setAttribute(
                            "routeFlowTableCount",
                            resultSet.getInt(1)
                    );
                }
            }

            String currentUser = String.valueOf(request.getAttribute("dbUser"));
            String container = String.valueOf(request.getAttribute("containerName"));
            Object countValue = request.getAttribute("routeFlowTableCount");
            int tableCount = countValue instanceof Integer
                    ? (Integer) countValue
                    : 0;

            boolean expectedSchema = "LJT_ROUTE_FLOW".equalsIgnoreCase(currentUser);
            boolean expectedContainer = "XEPDB1".equalsIgnoreCase(container);
            boolean completeSchema = tableCount == 10;

            request.setAttribute("expectedSchema", expectedSchema);
            request.setAttribute("expectedContainer", expectedContainer);
            request.setAttribute("completeSchema", completeSchema);
            request.setAttribute(
                    "databaseOk",
                    expectedSchema && expectedContainer && completeSchema
            );

        } catch (Exception exception) {
            request.setAttribute("databaseError", safeMessage(exception));
        }
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("(?i)(password\s*[=:]\s*)[^,;\s]+", "$1***");
    }
}
