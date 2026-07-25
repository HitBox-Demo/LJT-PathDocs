package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DepartmentDAO;
import com.chekrol.dms.dao.RoleRequestDAO;
import com.chekrol.dms.dao.UserDAO;
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

@WebServlet("/admin/users")
public class UserManagementServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final RoleRequestDAO roleRequestDAO = new RoleRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute(
                    "users",
                    AppConfig.isDemoMode() ? DemoData.users() : userDAO.listAll()
            );
            request.setAttribute(
                    "departments",
                    AppConfig.isDemoMode() ? DemoData.departments() : departmentDAO.listActive()
            );
            request.setAttribute("pageTitle", "User Management");
            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp")
                    .forward(request, response);
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User currentUser = (User) request.getSession().getAttribute("currentUser");
            if ("roleRequest".equals(request.getParameter("action"))) {
                createRoleRequest(request, currentUser);
                request.getSession().setAttribute(
                        "flashSuccess", "Role request sent to a boss for approval."
                );
            } else {
                createUser(request);
                request.getSession().setAttribute("flashSuccess", "User account created.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (Exception exception) {
            request.setAttribute("error", exception.getMessage());
            doGet(request, response);
        }
    }

    private void createRoleRequest(HttpServletRequest request, User currentUser) throws Exception {
        long userId = Long.parseLong(request.getParameter("userId"));
        String currentRole = ValidationUtil.clean(request.getParameter("currentRole"));
        String requestedRole = ValidationUtil.clean(request.getParameter("requestedRole"));
        String remarks = ValidationUtil.clean(request.getParameter("remarks"));

        if (AppConfig.isDemoMode()) {
            DemoData.createRoleRequest(userId, currentRole, requestedRole, currentUser, remarks);
        } else {
            roleRequestDAO.create(userId, currentRole, requestedRole, currentUser.getId(), remarks);
        }
    }

    private void createUser(HttpServletRequest request) throws Exception {
        User user = new User();
        user.setUsername(ValidationUtil.clean(request.getParameter("username")));
        user.setFullName(ValidationUtil.clean(request.getParameter("fullName")));
        user.setEmail(ValidationUtil.clean(request.getParameter("email")));
        String departmentId = request.getParameter("departmentId");
        user.setDepartmentId(
                departmentId == null || departmentId.isBlank()
                        ? null
                        : Long.valueOf(departmentId)
        );
        String temporaryPassword = request.getParameter("temporaryPassword");
        String roleCode = ValidationUtil.clean(request.getParameter("roleCode"));

        if (AppConfig.isDemoMode()) {
            DemoData.createUser(user, temporaryPassword, roleCode);
        } else {
            userDAO.createUser(user, temporaryPassword, roleCode);
        }
    }
}
