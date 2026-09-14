package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/documents/view")
public class DocumentViewServlet extends HttpServlet {

    private final DocumentDAO documentDAO = new DocumentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            long documentId = Long.parseLong(
                    request.getParameter("id")
            );

            User currentUser = (User) request
                    .getSession()
                    .getAttribute("currentUser");

            DocumentRecord document = AppConfig.isDemoMode()
                    ? DemoData.findDocument(
                            currentUser,
                            documentId
                    )
                    : documentDAO.findAuthorized(
                            currentUser,
                            documentId
                    );

            if (document == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
                return;
            }

            List<DocumentFile> files = AppConfig.isDemoMode()
                    ? DemoData.filesForDocument(
                            currentUser,
                            documentId
                    )
                    : documentDAO.listFiles(documentId);

            DocumentFile primaryFile = files.stream()
                    .filter(DocumentFile::isPrimaryFile)
                    .findFirst()
                    .orElse(null);

            request.setAttribute("document", document);
            request.setAttribute("files", files);
            request.setAttribute("primaryFile", primaryFile);
            request.setAttribute(
                    "pageTitle",
                    document.getDocumentCode()
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/documents/view.jsp"
            ).forward(request, response);

        } catch (NumberFormatException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid document ID."
            );

        } catch (Exception exception) {
            throw new ServletException(
                    "Unable to load the document.",
                    exception
            );
        }
    }
}
