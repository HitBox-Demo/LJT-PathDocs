package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/documents/action")
public class DocumentActionServlet extends HttpServlet {
    private final DocumentDAO documentDAO = new DocumentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            User currentUser = (User) request.getSession().getAttribute("currentUser");
            long documentId = Long.parseLong(request.getParameter("documentId"));
            String action = request.getParameter("action");

            if (!"RECALL".equals(action)) {
                throw new IllegalArgumentException("Unsupported document action.");
            }

            if (AppConfig.isDemoMode()) {
                DemoData.action(currentUser, documentId, "RECALL", null, null);
            } else {
                documentDAO.recall(currentUser, documentId);
            }

            request.getSession().setAttribute("flashSuccess", "Document recalled successfully.");
            response.sendRedirect(request.getContextPath() + "/documents/list?view=pending");
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID.");
        } catch (IllegalArgumentException exception) {
            request.getSession().setAttribute("flashError", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/documents/list?view=pending");
        } catch (Exception exception) {
            throw new ServletException("Unable to recall the document.", exception);
        }
    }
}
