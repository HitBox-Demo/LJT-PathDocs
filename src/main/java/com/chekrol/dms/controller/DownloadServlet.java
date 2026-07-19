package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.FileStorageUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/documents/download")
public class DownloadServlet extends HttpServlet {
    private final DocumentDAO dao=new DocumentDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{try{long fileId=Long.parseLong(req.getParameter("fileId"));User user=(User)req.getSession().getAttribute("currentUser");DocumentFile file=dao.findFileAuthorized(user,fileId);if(file==null){resp.sendError(404);return;}Path path=FileStorageUtil.resolveAuthorized(file.getStoragePath());resp.setHeader("Cache-Control","no-store, private");resp.setContentType(file.getMimeType()==null?"application/octet-stream":file.getMimeType());resp.setHeader("Content-Disposition","attachment; filename*=UTF-8''"+java.net.URLEncoder.encode(file.getOriginalName(),java.nio.charset.StandardCharsets.UTF_8).replace("+","%20"));resp.setContentLengthLong(Files.size(path));Files.copy(path,resp.getOutputStream());}catch(NumberFormatException ex){resp.sendError(400);}catch(Exception ex){throw new ServletException(ex);}}
}
