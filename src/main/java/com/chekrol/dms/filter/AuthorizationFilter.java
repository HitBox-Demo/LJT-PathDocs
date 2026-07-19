package com.chekrol.dms.filter;

import com.chekrol.dms.model.User;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class AuthorizationFilter implements Filter {
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest req=(HttpServletRequest)request;User user=(User)req.getSession().getAttribute("currentUser");String path=req.getRequestURI().substring(req.getContextPath().length());if(user==null){chain.doFilter(request,response);return;}boolean allowed=true;
        if(path.startsWith("/admin/"))allowed=user.hasRole("CLERK")||user.hasRole("SYSTEM_ADMIN");
        else if(path.startsWith("/approvals/"))allowed=user.hasRole("BOSS");
        else if(path.startsWith("/department/"))allowed=user.hasRole("DEPARTMENT_USER");
        else if(path.equals("/documents/create"))allowed=user.hasRole("CLERK")||user.hasRole("SYSTEM_ADMIN");
        if(!allowed){((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN);return;}chain.doFilter(request,response);
    }
}
