package com.chekrol.dms.controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{HttpSession s=req.getSession(false);if(s!=null)s.invalidate();resp.sendRedirect(req.getContextPath()+"/login?logout=1");}
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);}
}
