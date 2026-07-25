package com.chekrol.dms.controller;

import com.chekrol.dms.dao.NotificationDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/notifications/list")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        try {
            request.setAttribute(
                    "notifications",
                    AppConfig.isDemoMode()
                            ? DemoData.notifications(currentUser)
                            : notificationDAO.listForUser(currentUser.getId())
            );
            request.setAttribute("pageTitle", "Notifications");
            request.getRequestDispatcher("/WEB-INF/views/notifications/list.jsp")
                    .forward(request, response);
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        try {
            if (AppConfig.isDemoMode()) {
                DemoData.markNotificationsRead(currentUser);
            } else {
                notificationDAO.markAllRead(currentUser.getId());
            }
            request.getSession().setAttribute("flashSuccess", "Notifications marked as read.");
            response.sendRedirect(request.getContextPath() + "/notifications/list");
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }
}
