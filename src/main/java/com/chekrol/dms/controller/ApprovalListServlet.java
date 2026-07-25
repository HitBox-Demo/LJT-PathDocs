package com.chekrol.dms.controller;

import com.chekrol.dms.dao.ApprovalDAO;
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

@WebServlet("/approvals/list")
public class ApprovalListServlet extends HttpServlet {
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final ApprovalDAO approvalDAO = new ApprovalDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        try {
            if ("history".equals(request.getParameter("view"))) {
                request.setAttribute(
                        "approvals",
                        AppConfig.isDemoMode()
                                ? DemoData.approvals()
                                : approvalDAO.listHistory(currentUser)
                );
                request.setAttribute("pageTitle", "Approval History");
                request.getRequestDispatcher("/WEB-INF/views/approvals/history.jsp")
                        .forward(request, response);
                return;
            }

            request.setAttribute(
                    "documents",
                    AppConfig.isDemoMode()
                            ? DemoData.listDocuments(currentUser, "pending", "")
                            : documentDAO.listForUser(currentUser, "pending", "")
            );
            request.setAttribute("pageTitle", "Pending Approvals");
            request.getRequestDispatcher("/WEB-INF/views/approvals/list.jsp")
                    .forward(request, response);
        } catch (Exception exception) {
            throw new ServletException("Unable to load approval records.", exception);
        }
    }
}
