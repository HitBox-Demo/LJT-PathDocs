package com.chekrol.dms.controller;

import com.chekrol.dms.model.User;
import com.chekrol.dms.service.AuthenticationService;
import com.chekrol.dms.util.AppConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthenticationService authenticationService =
            new AuthenticationService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("currentUser") != null) {
            response.sendRedirect(
                    request.getContextPath() + "/app/dashboard"
            );
            return;
        }

        request.setAttribute(
                "demoMode",
                AppConfig.isDemoMode()
        );

        request.getRequestDispatcher(
                "/login.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            User user = authenticationService.authenticate(
                    request.getParameter("username"),
                    request.getParameter("password")
            );

            if (user == null) {
                request.setAttribute(
                        "error",
                        "Invalid username/password or inactive account."
                );
                doGet(request, response);
                return;
            }

            /*
             * Ensure role values from demo mode or Oracle are stored in
             * one consistent format before creating the session.
             */
            user.normaliseRoles();

            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("currentUser", user);
            newSession.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(
                    request.getContextPath() + "/app/dashboard"
            );

        } catch (Exception exception) {
            request.setAttribute(
                    "error",
                    "Login failed: " + exception.getMessage()
            );
            doGet(request, response);
        }
    }
}
