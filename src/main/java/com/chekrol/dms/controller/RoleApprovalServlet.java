package com.chekrol.dms.controller;

import com.chekrol.dms.dao.RoleRequestDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/approvals/role-requests")
public class RoleApprovalServlet extends HttpServlet {
    private final RoleRequestDAO dao=new RoleRequestDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{try{req.setAttribute("requests",AppConfig.isDemoMode()?DemoData.roleRequests():dao.listPending());req.setAttribute("pageTitle","Role Approval");req.getRequestDispatcher("/WEB-INF/views/approvals/role-requests.jsp").forward(req,resp);}catch(Exception ex){throw new ServletException(ex);}}
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{try{if(AppConfig.isDemoMode())throw new IllegalStateException("Role decisions are disabled in demo mode.");User boss=(User)req.getSession().getAttribute("currentUser");dao.decide(Long.parseLong(req.getParameter("requestId")),boss.getId(),req.getParameter("decision"),req.getParameter("remarks"));req.getSession().setAttribute("flashSuccess","Role request processed.");resp.sendRedirect(req.getContextPath()+"/approvals/role-requests");}catch(Exception ex){req.getSession().setAttribute("flashError",ex.getMessage());resp.sendRedirect(req.getContextPath()+"/approvals/role-requests");}}
}
