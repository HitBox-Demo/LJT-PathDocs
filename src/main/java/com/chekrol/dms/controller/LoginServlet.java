package com.chekrol.dms.controller;

import com.chekrol.dms.model.User;
import com.chekrol.dms.service.AuthenticationService;
import com.chekrol.dms.util.AppConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final AuthenticationService auth=new AuthenticationService();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(req.getSession(false)!=null&&req.getSession(false).getAttribute("currentUser")!=null){resp.sendRedirect(req.getContextPath()+"/app/dashboard");return;}
        req.setAttribute("demoMode",AppConfig.isDemoMode());req.getRequestDispatcher("/login.jsp").forward(req,resp);
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{User user=auth.authenticate(req.getParameter("username"),req.getParameter("password"));if(user==null){req.setAttribute("error","Invalid username/password or inactive account.");doGet(req,resp);return;}
            HttpSession old=req.getSession(false);if(old!=null)old.invalidate();HttpSession session=req.getSession(true);session.setAttribute("currentUser",user);session.setMaxInactiveInterval(30*60);resp.sendRedirect(req.getContextPath()+"/app/dashboard");
        }catch(Exception ex){req.setAttribute("error","Login failed: "+ex.getMessage());doGet(req,resp);}
    }
}
