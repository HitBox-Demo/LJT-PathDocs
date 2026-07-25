package com.chekrol.dms.controller;

import com.chekrol.dms.dao.RoleRequestDAO;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/role-requests")
public class RoleRequestServlet extends HttpServlet {
    private final RoleRequestDAO roleRequestDAO = new RoleRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute(
                    "requests",
                    AppConfig.isDemoMode()
                            ? DemoData.roleRequests()
                            : roleRequestDAO.listPending()
            );
            request.setAttribute("pageTitle", "Role Requests");
            request.getRequestDispatcher("/WEB-INF/views/admin/role-requests.jsp")
                    .forward(request, response);
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }
}
