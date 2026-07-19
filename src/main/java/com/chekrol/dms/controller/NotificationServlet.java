package com.chekrol.dms.controller;

import com.chekrol.dms.dao.NotificationDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/notifications/list")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO dao=new NotificationDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{User u=(User)req.getSession().getAttribute("currentUser");try{req.setAttribute("notifications",AppConfig.isDemoMode()?DemoData.notifications(u):dao.listForUser(u.getId()));req.setAttribute("pageTitle","Notifications");req.getRequestDispatcher("/WEB-INF/views/notifications/list.jsp").forward(req,resp);}catch(Exception ex){throw new ServletException(ex);}}
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{User u=(User)req.getSession().getAttribute("currentUser");try{if(!AppConfig.isDemoMode())dao.markAllRead(u.getId());resp.sendRedirect(req.getContextPath()+"/notifications/list");}catch(Exception ex){throw new ServletException(ex);}}
}
