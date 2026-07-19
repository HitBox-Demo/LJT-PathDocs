package com.chekrol.dms.controller;

import com.chekrol.dms.dao.RoleRequestDAO;
import com.chekrol.dms.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/role-requests")
public class RoleRequestServlet extends HttpServlet {
    private final RoleRequestDAO dao=new RoleRequestDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws 
    ServletException,IOException
    {try{req.setAttribute("requests",
    AppConfig.isDemoMode()?DemoData.roleRequests():
    dao.listPending());req.setAttribute("pageTitle","Role Requests");
    req.getRequestDispatcher("/WEB-INF/views/admin/role-requests.jsp").forward(req,resp);}
    catch(Exception ex){throw new ServletException(ex);}}
}
