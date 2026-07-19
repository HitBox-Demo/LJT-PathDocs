package com.chekrol.dms.controller;

import com.chekrol.dms.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/setup/database-test")
public class DatabaseTestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{req.setAttribute("pageTitle","Database Test");req.setAttribute("demoMode",AppConfig.isDemoMode());req.setAttribute("configSource",DatabaseConnection.configurationSource());if(!AppConfig.isDemoMode()){try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT SYS_CONTEXT('USERENV','DB_NAME'),SYS_CONTEXT('USERENV','SERVICE_NAME'),SYS_CONTEXT('USERENV','SERVER_HOST'),USER FROM dual");ResultSet rs=ps.executeQuery()){if(rs.next()){req.setAttribute("dbName",rs.getString(1));req.setAttribute("serviceName",rs.getString(2));req.setAttribute("serverHost",rs.getString(3));req.setAttribute("dbUser",rs.getString(4));req.setAttribute("databaseOk",true);}}catch(Exception ex){req.setAttribute("databaseError",ex.getMessage());}}req.getRequestDispatcher("/WEB-INF/views/setup/database-test.jsp").forward(req,resp);}
}
