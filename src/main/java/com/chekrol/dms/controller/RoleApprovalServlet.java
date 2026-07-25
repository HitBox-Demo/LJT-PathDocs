package com.chekrol.dms.controller;

import com.chekrol.dms.dao.RoleRequestDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import com.chekrol.dms.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/approvals/role-requests")
public class RoleApprovalServlet extends HttpServlet {
    private final RoleRequestDAO roleRequestDAO = new RoleRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute(
                    "requests",
                    AppConfig.isDemoMode()
                            ? DemoData.pendingRoleRequests()
                            : roleRequestDAO.listPending()
            );
            request.setAttribute("pageTitle", "Role Approval");
            request.getRequestDispatcher("/WEB-INF/views/approvals/role-requests.jsp")
                    .forward(request, response);
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            User boss = (User) request.getSession().getAttribute("currentUser");
            long requestId = Long.parseLong(request.getParameter("requestId"));
            String decision = ValidationUtil.clean(request.getParameter("decision")).toUpperCase();
            String remarks = ValidationUtil.clean(request.getParameter("remarks"));

            if (AppConfig.isDemoMode()) {
                DemoData.decideRoleRequest(requestId, boss, decision, remarks);
            } else {
                roleRequestDAO.decide(requestId, boss.getId(), decision, remarks);
            }
            request.getSession().setAttribute("flashSuccess", "Role request processed.");
        } catch (Exception exception) {
            request.getSession().setAttribute("flashError", exception.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/approvals/role-requests");
    }
}
