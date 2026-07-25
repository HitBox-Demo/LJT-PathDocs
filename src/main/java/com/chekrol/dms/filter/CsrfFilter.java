package com.chekrol.dms.filter;

import com.chekrol.dms.util.CsrfUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CsrfFilter implements Filter {

    private static final String TOKEN_PARAMETER = "csrfToken";
    private static final String TOKEN_HEADER = "X-CSRF-Token";
    private static final int MAX_TOKEN_BYTES = 512;

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

        if (isSafeMethod(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Your session has expired. Please sign in again."
            );
            return;
        }

        String suppliedToken = resolveToken(httpRequest);

        if (!CsrfUtil.isValid(session, suppliedToken)) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid or expired CSRF token."
            );
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Resolve a token without relying only on getParameter().
     *
     * For normal URL-encoded forms, getParameter() is sufficient.
     * For multipart uploads, the hidden csrfToken field is itself a Part.
     */
    private String resolveToken(
            HttpServletRequest request
    ) throws IOException, ServletException {

        String headerToken = request.getHeader(TOKEN_HEADER);

        if (hasText(headerToken)) {
            return headerToken;
        }

        if (!isMultipart(request)) {
            return request.getParameter(TOKEN_PARAMETER);
        }

        for (Part part : request.getParts()) {
            if (!TOKEN_PARAMETER.equals(part.getName())) {
                continue;
            }

            if (part.getSize() <= 0 || part.getSize() > MAX_TOKEN_BYTES) {
                return null;
            }

            return new String(
                    part.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();
        }

        return null;
    }

    private boolean isMultipart(
            HttpServletRequest request
    ) {
        String contentType = request.getContentType();

        return contentType != null
                && contentType
                        .toLowerCase(Locale.ROOT)
                        .startsWith("multipart/form-data");
    }

    private boolean isSafeMethod(String method) {
        return "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
