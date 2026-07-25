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
            SELECT SYS_CONTEXT('USERENV','DB_NAME'),
                   SYS_CONTEXT('USERENV','SERVICE_NAME'),
                   SYS_CONTEXT('USERENV','SERVER_HOST'),
                   USER
            FROM dual
            """;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Database Test");
        request.setAttribute("demoMode", AppConfig.isDemoMode());
        request.setAttribute("configSource", DatabaseConnection.configurationSource());

        if (!AppConfig.isDemoMode()) {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(DATABASE_INFO_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    request.setAttribute("dbName", resultSet.getString(1));
                    request.setAttribute("serviceName", resultSet.getString(2));
                    request.setAttribute("serverHost", resultSet.getString(3));
                    request.setAttribute("dbUser", resultSet.getString(4));
                    request.setAttribute("databaseOk", true);
                }
            } catch (Exception exception) {
                request.setAttribute("databaseError", exception.getMessage());
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/setup/database-test.jsp")
                .forward(request, response);
    }
}
