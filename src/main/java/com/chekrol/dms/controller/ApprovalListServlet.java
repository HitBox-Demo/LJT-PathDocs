package com.chekrol.dms.controller;

import com.chekrol.dms.dao.ApprovalDAO;
import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/approvals/list")
public class ApprovalListServlet extends HttpServlet {
    private final DocumentDAO documents=new DocumentDAO();private final ApprovalDAO approvals=new ApprovalDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{User user=(User)req.getSession().getAttribute("currentUser");try{if("history".equals(req.getParameter("view"))){req.setAttribute("approvals",AppConfig.isDemoMode()?DemoData.approvals():approvals.listHistory(user));req.setAttribute("pageTitle","Approval History");req.getRequestDispatcher("/WEB-INF/views/approvals/history.jsp").forward(req,resp);}else{req.setAttribute("documents",AppConfig.isDemoMode()?DemoData.listDocuments(user,"pending",""):documents.listForUser(user,"pending",""));req.setAttribute("pageTitle","Pending Approvals");req.getRequestDispatcher("/WEB-INF/views/approvals/list.jsp").forward(req,resp);}}catch(Exception ex){throw new ServletException(ex);}}
}
