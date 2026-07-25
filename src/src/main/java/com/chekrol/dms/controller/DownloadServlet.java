package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import com.chekrol.dms.util.FileStorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/documents/download")
public class DownloadServlet extends HttpServlet {
    private final DocumentDAO dao = new DocumentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            long fileId = Long.parseLong(request.getParameter("fileId"));
            User user = (User) request.getSession().getAttribute("currentUser");
            DocumentFile file = AppConfig.isDemoMode()
                    ? DemoData.findFileAuthorized(user, fileId)
                    : dao.findFileAuthorized(user, fileId);

            if (file == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Path path = FileStorageUtil.resolveAuthorized(file.getStoragePath());
            response.setHeader("Cache-Control", "no-store, private");
            response.setContentType(file.getMimeType() == null
                    ? "application/octet-stream"
                    : file.getMimeType());
            boolean inline = "true".equalsIgnoreCase(request.getParameter("inline"))
                    && file.getMimeType() != null
                    && file.getMimeType().startsWith("image/");
            response.setHeader(
                    "Content-Disposition",
                    (inline ? "inline" : "attachment") + "; filename*=UTF-8''"
                            + URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20")
            );
            response.setContentLengthLong(Files.size(path));
            Files.copy(path, response.getOutputStream());
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }
}
