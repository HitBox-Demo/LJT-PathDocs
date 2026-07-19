package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/app/dashboard")
public class DashboardServlet extends HttpServlet {
    private final DocumentDAO documents=new DocumentDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=(User)req.getSession().getAttribute("currentUser");try{
            req.setAttribute("stats",AppConfig.isDemoMode()?DemoData.stats(user):documents.dashboardStats(user));
            req.setAttribute("documents",AppConfig.isDemoMode()?DemoData.listDocuments(user,"repository",""):documents.listForUser(user,"repository",""));
            req.setAttribute("pageTitle","Dashboard");req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req,resp);
        }catch(Exception ex){throw new ServletException(ex);}
    }
}
