package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/documents/action")
public class DocumentActionServlet extends HttpServlet {
    private final DocumentDAO dao=new DocumentDAO();
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{try{User user=(User)req.getSession().getAttribute("currentUser");long id=Long.parseLong(req.getParameter("documentId"));String action=req.getParameter("action");if(!"RECALL".equals(action))throw new IllegalArgumentException("Unsupported document action.");if(AppConfig.isDemoMode())DemoData.action(user,id,"RECALL",null,null);else dao.recall(user,id);req.getSession().setAttribute("flashSuccess","Document recalled successfully.");resp.sendRedirect(req.getContextPath()+"/documents/list?view=pending");}catch(Exception ex){throw new ServletException(ex);}}
}
