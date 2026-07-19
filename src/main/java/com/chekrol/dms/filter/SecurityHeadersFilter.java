package com.chekrol.dms.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeadersFilter implements Filter {
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletResponse res=(HttpServletResponse)response;
        res.setHeader("X-Content-Type-Options","nosniff");
        res.setHeader("X-Frame-Options","DENY");
        res.setHeader("Referrer-Policy","same-origin");
        res.setHeader("Permissions-Policy","camera=(self), microphone=(), geolocation=()");
        chain.doFilter(request,response);
    }
}
