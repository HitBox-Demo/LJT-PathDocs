package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/documents/list")
public class DocumentListServlet extends HttpServlet {
    private final DocumentDAO dao=new DocumentDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=(User)req.getSession().getAttribute("currentUser");String view=req.getParameter("view");if(view==null)view="repository";String q=req.getParameter("q");
        try{req.setAttribute("documents",AppConfig.isDemoMode()?DemoData.listDocuments(user,view,q):dao.listForUser(user,view,q));req.setAttribute("view",view);req.setAttribute("query",q);req.setAttribute("pageTitle",switch(view){case"draft"->"Draft Documents";case"pending"->"Pending Documents";case"returned"->"Returned for Correction";case"department"->user.getDepartmentName()+" Folder";case"search"->"Search Documents";default->"Document Repository";});req.getRequestDispatcher("/WEB-INF/views/documents/list.jsp").forward(req,resp);}catch(Exception ex){throw new ServletException(ex);}
    }
}
