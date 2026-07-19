package com.chekrol.dms.filter;

import com.chekrol.dms.util.CsrfUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class CsrfFilter implements Filter {
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest req=(HttpServletRequest)request;
        if(req.getSession(false)==null){chain.doFilter(request,response);return;}
        if(!"GET".equalsIgnoreCase(req.getMethod())&&!"HEAD".equalsIgnoreCase(req.getMethod())){
            if(!CsrfUtil.isValid(req.getSession(),req.getParameter("csrfToken"))){((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid or expired CSRF token.");return;}
        }
        chain.doFilter(request,response);
    }
}
