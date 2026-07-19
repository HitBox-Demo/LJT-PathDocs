package com.chekrol.dms.controller;

import com.chekrol.dms.model.User;
import com.chekrol.dms.service.ApprovalService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/approvals/action")
public class ApprovalActionServlet extends HttpServlet {
    private final ApprovalService service=new ApprovalService();
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,
    ServletException{try{User user=(User)req.getSession().getAttribute("currentUser");
    String decision=req.getParameter("decision");String remarks=req.getParameter("remarks");
    Long dept=req.getParameter("departmentId")==null||req.getParameter("departmentId").isBlank()?null:Long.valueOf(req.getParameter("departmentId"));
    String[] ids=req.getParameterValues("documentId");if(ids==null)throw new IllegalArgumentException("No document selected.");
    for(String id:ids)service.decide(user,Long.parseLong(id),decision,remarks,dept);req.getSession().setAttribute("flashSuccess",ids.length+" document(s) processed.");
    resp.sendRedirect(req.getContextPath()+"/approvals/list");}catch(Exception ex){throw new ServletException(ex);}}
}
