package com.chekrol.dms.filter;

import com.chekrol.dms.model.User;
import com.chekrol.dms.util.CsrfUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class AuthenticationFilter implements Filter {
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest req=(HttpServletRequest)request;HttpServletResponse res=(HttpServletResponse)response;
        HttpSession session=req.getSession(false);User user=session==null?null:(User)session.getAttribute("currentUser");
        if(user==null){String target=req.getRequestURI();res.sendRedirect(req.getContextPath()+"/login?expired=1&target="+java.net.URLEncoder.encode(target,java.nio.charset.StandardCharsets.UTF_8));return;}
        req.setAttribute("currentUser",user);req.setAttribute("csrfToken",CsrfUtil.ensureToken(session));chain.doFilter(request,response);
    }
}
