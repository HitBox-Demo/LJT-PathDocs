package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/documents/view")
public class DocumentViewServlet extends HttpServlet {
    private final DocumentDAO dao=new DocumentDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{long id=Long.parseLong(req.getParameter("id"));User user=(User)req.getSession().getAttribute("currentUser");var doc=AppConfig.isDemoMode()?DemoData.findDocument(user,id):dao.findAuthorized(user,id);if(doc==null){resp.sendError(404);return;}req.setAttribute("document",doc);req.setAttribute("files",AppConfig.isDemoMode()?java.util.List.of():dao.listFiles(id));req.setAttribute("pageTitle",doc.getDocumentCode());req.getRequestDispatcher("/WEB-INF/views/documents/view.jsp").forward(req,resp);}catch(NumberFormatException ex){resp.sendError(400,"Invalid document ID.");}catch(Exception ex){throw new ServletException(ex);}
    }
}
