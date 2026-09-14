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
import java.util.Set;

@WebServlet("/documents/list")
public class DocumentListServlet extends HttpServlet {

    private static final Set<String> ALLOWED_VIEWS = Set.of(
            "repository",
            "draft",
            "pending",
            "overdue",
            "routed",
            "returned",
            "department",
            "search"
    );

    private final DocumentDAO documentDAO = new DocumentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User currentUser = (User) request
                .getSession()
                .getAttribute("currentUser");

        String requestedView = request.getParameter("view");

        String view = ALLOWED_VIEWS.contains(requestedView)
                ? requestedView
                : "repository";

        String query = request.getParameter("q");

        try {
            request.setAttribute(
                    "documents",
                    AppConfig.isDemoMode()
                            ? DemoData.listDocuments(
                                    currentUser,
                                    view,
                                    query
                            )
                            : documentDAO.listForUser(
                                    currentUser,
                                    view,
                                    query
                            )
            );

            request.setAttribute("view", view);
            request.setAttribute("query", query);
            request.setAttribute(
                    "pageTitle",
                    pageTitle(view, currentUser)
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/documents/list.jsp"
            ).forward(request, response);

        } catch (Exception exception) {
            throw new ServletException(
                    "Unable to load documents.",
                    exception
            );
        }
    }

    private String pageTitle(String view, User user) {
        return switch (view) {
            case "draft" -> "Draft Documents";
            case "pending" -> "Pending Documents";
            case "overdue" -> "Overdue Documents";
            case "routed" -> "Routed Documents";
            case "returned" -> "Returned for Correction";
            case "department" -> (
                    user.getDepartmentName() == null
                            ? "Department"
                            : user.getDepartmentName()
            ) + " Folder";
            case "search" -> "Search Documents";
            default -> "Document Repository";
        };
    }
}
