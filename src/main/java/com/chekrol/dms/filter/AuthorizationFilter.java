package com.chekrol.dms.filter;

import com.chekrol.dms.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        User currentUser = session == null
                ? null
                : (User) session.getAttribute("currentUser");

        /*
         * AuthenticationFilter handles missing sessions. Do not turn
         * a missing session into a misleading role-based 403 here.
         */
        if (currentUser == null) {
            chain.doFilter(request, response);
            return;
        }

        currentUser.normaliseRoles();

        String path = normalisePath(
                httpRequest.getRequestURI(),
                httpRequest.getContextPath()
        );

        if (!isAllowed(path, currentUser)) {
            /*
             * The configured Tomcat error-page mapping can render the
             * project's friendly /WEB-INF/views/errors/403.jsp page.
             */
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowed(String path, User user) {

        /*
         * Clerk and System Administrator are both allowed to manage
         * the document-submission lifecycle.
         */
        if (isDocumentManagementPath(path)) {
            return user.canManageDocuments();
        }

        /*
         * User administration remains available to the combined
         * Clerk/System Administrator role used by this project.
         */
        if (path.startsWith("/admin/")) {
            return user.canManageUsers();
        }

        if (path.startsWith("/approvals/")) {
            return user.canApproveDocuments();
        }

        if (path.startsWith("/department/")) {
            return user.canViewDepartmentFolder();
        }

        return true;
    }

    private boolean isDocumentManagementPath(String path) {
        return path.equals("/documents/create")
                || path.startsWith("/documents/create/")
                || path.equals("/documents/edit")
                || path.startsWith("/documents/edit/")
                || path.equals("/documents/recall")
                || path.startsWith("/documents/recall/")
                || path.equals("/documents/upload")
                || path.startsWith("/documents/upload/");
    }

    private String normalisePath(
            String requestUri,
            String contextPath
    ) {
        String path = requestUri;

        if (contextPath != null
                && !contextPath.isEmpty()
                && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        /*
         * Remove URL-rewritten session suffixes such as:
         * /documents/create;jsessionid=...
         */
        int semicolonIndex = path.indexOf(';');

        if (semicolonIndex >= 0) {
            path = path.substring(0, semicolonIndex);
        }

        while (path.contains("//")) {
            path = path.replace("//", "/");
        }

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path.isEmpty() ? "/" : path;
    }
}
