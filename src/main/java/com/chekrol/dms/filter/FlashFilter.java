package com.chekrol.dms.filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class FlashFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest)request;
        HttpSession session=req.getSession(false);
        if(session!=null){
            Object success=session.getAttribute("flashSuccess");
            Object error=session.getAttribute("flashError");
            if(success!=null){req.setAttribute("flashSuccess",success);session.removeAttribute("flashSuccess");}
            if(error!=null){req.setAttribute("flashError",error);session.removeAttribute("flashError");}
        }
        chain.doFilter(request,response);
    }
}
