package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DepartmentDAO;
import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/approvals/review")
public class ApprovalReviewServlet extends HttpServlet {
    private final DocumentDAO documents=new DocumentDAO();private final DepartmentDAO departments=new DepartmentDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{try{long id=Long.parseLong(req.getParameter("id"));User user=(User)req.getSession().getAttribute("currentUser");var doc=AppConfig.isDemoMode()?DemoData.findDocument(user,id):documents.findAuthorized(user,id);if(doc==null){resp.sendError(404);return;}req.setAttribute("document",doc);req.setAttribute("departments",AppConfig.isDemoMode()?DemoData.departments():departments.listActive());req.setAttribute("files",AppConfig.isDemoMode()?java.util.List.of():documents.listFiles(id));req.setAttribute("pageTitle","Review "+doc.getDocumentCode());req.getRequestDispatcher("/WEB-INF/views/approvals/review.jsp").forward(req,resp);}catch(Exception ex){throw new ServletException(ex);}}
}
