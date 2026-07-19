package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DepartmentDAO;
import com.chekrol.dms.dao.UserDAO;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.service.DocumentService;
import com.chekrol.dms.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/documents/create")
@MultipartConfig(maxFileSize=50L*1024*1024,maxRequestSize=150L*1024*1024)
public class DocumentCreateServlet extends HttpServlet {
    private final DepartmentDAO departments=new DepartmentDAO();private final UserDAO users=new UserDAO();private final DocumentService service=new DocumentService();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{try{req.setAttribute("departments",AppConfig.isDemoMode()?DemoData.departments():departments.listActive());req.setAttribute("bosses",AppConfig.isDemoMode()?DemoData.bosses():users.listBosses());req.setAttribute("pageTitle","Create Document");req.getRequestDispatcher("/WEB-INF/views/documents/create.jsp").forward(req,resp);}catch(Exception ex){throw new ServletException(ex);}}
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{User user=(User)req.getSession().getAttribute("currentUser");String title=ValidationUtil.clean(req.getParameter("title"));String reference=ValidationUtil.clean(req.getParameter("referenceNo"));String sender=ValidationUtil.clean(req.getParameter("sender"));if(!ValidationUtil.hasText(title)||!ValidationUtil.hasText(reference)||!ValidationUtil.hasText(sender))throw new IllegalArgumentException("Title, reference number and sender are required.");
            DocumentRecord 
            d=new DocumentRecord();d.setTitle(title);d.setReferenceNo(reference);
            d.setSender(sender);d.setDateReceived(LocalDate.parse(req.getParameter("dateReceived")));
            d.setCategory(ValidationUtil.clean(req.getParameter("category")));
            d.setPriority("URGENT".equals(req.getParameter("priority"))?"URGENT":"NORMAL");
            d.setConfidential(false);
            d.setDescription(ValidationUtil.clean(req.getParameter("description")));
            d.setDestinationDepartmentId(Long.valueOf(req.getParameter("departmentId")));
            d.setBossId(Long.valueOf(req.getParameter("bossId")));
            List<Part> images=req.getParts().stream().filter(p->"captureImages".equals(p.getName())).toList();List<Part> attachments=req.getParts().stream().filter(p->"attachments".equals(p.getName())).toList();Part primary=req.getPart("primaryPdf");boolean submit="submit".equals(req.getParameter("action"));long id=service.create(user,d,images,primary,attachments,submit);req.getSession().setAttribute("flashSuccess",submit?"Document submitted for approval.":"Draft saved.");resp.sendRedirect(req.getContextPath()+"/documents/view?id="+id);
        }catch(Exception ex){req.setAttribute("error",ex.getMessage());doGet(req,resp);}
    }
}
