package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/app/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Set<String> ALLOWED_FILTERS =
            Set.of("total", "pending", "overdue", "routed");

    private final DocumentDAO documentDAO = new DocumentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User currentUser = (User) request
                .getSession()
                .getAttribute("currentUser");

        String selectedFilter = normaliseFilter(
                request.getParameter("filter")
        );

        String documentView = switch (selectedFilter) {
            case "pending" -> "pending";
            case "overdue" -> "overdue";
            case "routed" -> "routed";
            default -> "repository";
        };

        try {
            Map<String, Long> statistics = AppConfig.isDemoMode()
                    ? DemoData.stats(currentUser)
                    : documentDAO.dashboardStats(currentUser);

            List<DocumentRecord> documents = AppConfig.isDemoMode()
                    ? DemoData.listDocuments(
                            currentUser,
                            documentView,
                            ""
                    )
                    : documentDAO.listForUser(
                            currentUser,
                            documentView,
                            ""
                    );

            request.setAttribute("stats", statistics);
            request.setAttribute("documents", documents);
            request.setAttribute("selectedFilter", selectedFilter);
            request.setAttribute(
                    "selectedFilterLabel",
                    filterLabel(selectedFilter)
            );
            request.setAttribute(
                    "dashboardListView",
                    documentView
            );
            request.setAttribute("pageTitle", "Dashboard");

            request.getRequestDispatcher(
                    "/WEB-INF/views/dashboard.jsp"
            ).forward(request, response);

        } catch (Exception exception) {
            throw new ServletException(
                    "Unable to load the dashboard.",
                    exception
            );
        }
    }

    private String normaliseFilter(String requestedFilter) {
        if (requestedFilter == null) {
            return "total";
        }

        String filter = requestedFilter
                .trim()
                .toLowerCase(Locale.ROOT);

        return ALLOWED_FILTERS.contains(filter)
                ? filter
                : "total";
    }

    private String filterLabel(String filter) {
        return switch (filter) {
            case "pending" -> "Pending documents";
            case "overdue" -> "Overdue documents";
            case "routed" -> "Routed documents";
            default -> "All visible documents";
        };
    }
}
