package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DepartmentDAO;
import com.chekrol.dms.dao.UserDAO;
import com.chekrol.dms.dao.RoleRequestDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/users")
public class UserManagementServlet extends HttpServlet {
    private final UserDAO users=new UserDAO();private final DepartmentDAO departments=new DepartmentDAO();private final RoleRequestDAO roleRequests=new RoleRequestDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{try{req.setAttribute("users",AppConfig.isDemoMode()?DemoData.users():users.listAll());req.setAttribute("departments",AppConfig.isDemoMode()?DemoData.departments():departments.listActive());req.setAttribute("pageTitle","User Management");req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req,resp);}catch(Exception ex){throw new ServletException(ex);}}
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{try{if(AppConfig.isDemoMode())throw new IllegalStateException("User and role changes are disabled in demo mode. Connect Oracle first.");User current=(User)req.getSession().getAttribute("currentUser");
        if("roleRequest".equals(req.getParameter("action"))){roleRequests.create(Long.parseLong(req.getParameter("userId")),req.getParameter("currentRole"),req.getParameter("requestedRole"),current.getId(),req.getParameter("remarks"));req.getSession().setAttribute("flashSuccess","Role request sent to a boss for approval.");}
        else{User u=new User();u.setUsername(ValidationUtil.clean(req.getParameter("username")));u.setFullName(ValidationUtil.clean(req.getParameter("fullName")));u.setEmail(ValidationUtil.clean(req.getParameter("email")));String dep=req.getParameter("departmentId");u.setDepartmentId(dep==null||dep.isBlank()?null:Long.valueOf(dep));users.createUser(u,req.getParameter("temporaryPassword"),req.getParameter("roleCode"));req.getSession().setAttribute("flashSuccess","User account created.");}
        resp.sendRedirect(req.getContextPath()+"/admin/users");}catch(Exception ex){req.setAttribute("error",ex.getMessage());doGet(req,resp);}}
}
