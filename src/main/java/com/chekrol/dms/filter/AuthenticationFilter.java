package com.chekrol.dms.filter;

import com.chekrol.dms.model.User;
import com.chekrol.dms.util.CsrfUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthenticationFilter implements Filter {

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

        if (currentUser == null) {
            String target = httpRequest.getRequestURI();

            String encodedTarget = URLEncoder.encode(
                    target,
                    StandardCharsets.UTF_8
            );

            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                            + "/login?expired=1&target="
                            + encodedTarget
            );
            return;
        }

        /*
         * Normalise roles from demo mode, an older session, or Oracle.
         * This prevents "clerk", " CLERK " and "CLERK" from behaving
         * as different permissions.
         */
        currentUser.normaliseRoles();

        httpRequest.setAttribute("currentUser", currentUser);
        httpRequest.setAttribute(
                "csrfToken",
                CsrfUtil.ensureToken(session)
        );

        /*
         * Explicit permission flags are safer in JSP than invoking
         * hasRole(...) directly from Expression Language.
         */
        httpRequest.setAttribute(
                "canManageDocuments",
                currentUser.canManageDocuments()
        );

        httpRequest.setAttribute(
                "canManageUsers",
                currentUser.canManageUsers()
        );

        httpRequest.setAttribute(
                "canApproveDocuments",
                currentUser.canApproveDocuments()
        );

        httpRequest.setAttribute(
                "canViewDepartmentFolder",
                currentUser.canViewDepartmentFolder()
        );

        chain.doFilter(request, response);
    }
}
