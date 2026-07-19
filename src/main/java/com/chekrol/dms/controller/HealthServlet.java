package com.chekrol.dms.controller;

import com.chekrol.dms.util.AppConfig;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf(
                "{\"status\":\"UP\",\"application\":\"LJT RouteFlow\",\"demoMode\":%s}",
                AppConfig.isDemoMode()
        );
    }
}
