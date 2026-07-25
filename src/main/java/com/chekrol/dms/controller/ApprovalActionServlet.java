package com.chekrol.dms.controller;

import com.chekrol.dms.model.User;
import com.chekrol.dms.service.ApprovalService;
import com.chekrol.dms.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/approvals/action")
public class ApprovalActionServlet extends HttpServlet {
    private final ApprovalService approvalService = new ApprovalService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            User currentUser = (User) request.getSession().getAttribute("currentUser");
            String decision = ValidationUtil.clean(request.getParameter("decision")).toUpperCase();
            String remarks = ValidationUtil.clean(request.getParameter("remarks"));
            Long departmentId = parseOptionalId(request.getParameter("departmentId"));
            String[] documentIds = request.getParameterValues("documentId");

            if (documentIds == null || documentIds.length == 0) {
                throw new IllegalArgumentException("No document selected.");
            }

            for (String documentId : documentIds) {
                approvalService.decide(
                        currentUser,
                        Long.parseLong(documentId),
                        decision,
                        remarks,
                        departmentId
                );
            }

            request.getSession().setAttribute(
                    "flashSuccess",
                    documentIds.length + " document(s) processed."
            );
            response.sendRedirect(request.getContextPath() + "/approvals/list");
        } catch (IllegalArgumentException exception) {
            request.getSession().setAttribute("flashError", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/approvals/list");
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    private Long parseOptionalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
