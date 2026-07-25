package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DepartmentDAO;
import com.chekrol.dms.dao.UserDAO;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.service.DocumentService;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import com.chekrol.dms.util.SubmissionGuard;
import com.chekrol.dms.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@WebServlet("/documents/create")
@MultipartConfig(
        maxFileSize = 50L * 1024 * 1024,
        maxRequestSize = 150L * 1024 * 1024
)
public class DocumentCreateServlet extends HttpServlet {

    private static final String FORM_KEY = "create-document";
    private static final String IMAGE_FIELD = "captureImages";
    private static final String PRIMARY_PDF_FIELD = "primaryPdf";
    private static final String ATTACHMENT_FIELD = "attachments";

    private final DepartmentDAO departmentDAO =
            new DepartmentDAO();

    private final UserDAO userDAO =
            new UserDAO();

    private final DocumentService documentService =
            new DocumentService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            loadFormOptions(request);

            request.setAttribute(
                    "submissionToken",
                    SubmissionGuard.issueToken(
                            request.getSession(),
                            FORM_KEY
                    )
            );

            request.setAttribute(
                    "pageTitle",
                    "Create Document"
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/documents/create.jsp"
            ).forward(request, response);

        } catch (Exception exception) {
            throw new ServletException(
                    "Unable to load the Create Document page.",
                    exception
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/login?expired=1"
            );
            return;
        }

        String submittedToken =
                request.getParameter("submissionToken");

        if (!SubmissionGuard.begin(
                session,
                FORM_KEY,
                submittedToken
        )) {
            response.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    "This document is already being submitted or the form has expired."
            );
            return;
        }

        try {
            User currentUser = getCurrentUser(request);
            DocumentRecord document = buildDocument(request);

            Collection<Part> allParts = request.getParts();

            List<Part> images = allParts.stream()
                    .filter(part ->
                            IMAGE_FIELD.equals(part.getName())
                    )
                    .filter(DocumentCreateServlet::hasUploadedFile)
                    .toList();

            validateCapturedImages(images);

            Part primaryPdf = allParts.stream()
                    .filter(part ->
                            PRIMARY_PDF_FIELD.equals(part.getName())
                    )
                    .filter(DocumentCreateServlet::hasUploadedFile)
                    .findFirst()
                    .orElse(null);

            List<Part> attachments = allParts.stream()
                    .filter(part ->
                            ATTACHMENT_FIELD.equals(part.getName())
                    )
                    .filter(DocumentCreateServlet::hasUploadedFile)
                    .toList();

            boolean submit = "submit".equals(
                    request.getParameter("action")
            );

            long documentId = documentService.create(
                    currentUser,
                    document,
                    images,
                    primaryPdf,
                    attachments,
                    submit
            );

            SubmissionGuard.complete(
                    session,
                    FORM_KEY
            );

            session.setAttribute(
                    "flashSuccess",
                    submit
                            ? "Document submitted for approval."
                            : "Draft saved."
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/documents/view?id="
                            + documentId
            );

        } catch (Exception exception) {
            SubmissionGuard.fail(
                    session,
                    FORM_KEY
            );

            request.setAttribute(
                    "error",
                    exception.getMessage()
            );

            doGet(request, response);
        }
    }

    private void loadFormOptions(
            HttpServletRequest request
    ) throws Exception {

        if (AppConfig.isDemoMode()) {
            request.setAttribute(
                    "departments",
                    DemoData.departments()
            );

            request.setAttribute(
                    "bosses",
                    DemoData.bosses()
            );

            return;
        }

        request.setAttribute(
                "departments",
                departmentDAO.listActive()
        );

        request.setAttribute(
                "bosses",
                userDAO.listBosses()
        );
    }

    private User getCurrentUser(
            HttpServletRequest request
    ) {
        User currentUser = (User) request
                .getSession()
                .getAttribute("currentUser");

        if (currentUser == null) {
            throw new IllegalStateException(
                    "Your session has expired. Please sign in again."
            );
        }

        if (!currentUser.hasRole("CLERK")
                && !currentUser.hasRole("SYSTEM_ADMIN")) {
            throw new IllegalStateException(
                    "You do not have permission to create documents."
            );
        }

        return currentUser;
    }

    private DocumentRecord buildDocument(
            HttpServletRequest request
    ) {
        String title = ValidationUtil.clean(
                request.getParameter("title")
        );

        String referenceNumber = ValidationUtil.clean(
                request.getParameter("referenceNo")
        );

        String sender = ValidationUtil.clean(
                request.getParameter("sender")
        );

        String dateReceivedText = ValidationUtil.clean(
                request.getParameter("dateReceived")
        );

        if (!ValidationUtil.hasText(title)
                || !ValidationUtil.hasText(referenceNumber)
                || !ValidationUtil.hasText(sender)
                || !ValidationUtil.hasText(dateReceivedText)) {

            throw new IllegalArgumentException(
                    "Title, reference number, sender and date received are required."
            );
        }

        String category = ValidationUtil.clean(
                request.getParameter("category")
        );

        if (!ValidationUtil.hasText(category)) {
            throw new IllegalArgumentException(
                    "Please select a document category."
            );
        }

        DocumentRecord document = new DocumentRecord();

        document.setTitle(title);
        document.setReferenceNo(referenceNumber);
        document.setSender(sender);
        document.setDateReceived(
                LocalDate.parse(dateReceivedText)
        );
        document.setCategory(category);
        document.setPriority(
                "URGENT".equals(
                        request.getParameter("priority")
                )
                        ? "URGENT"
                        : "NORMAL"
        );
        // document.setConfidential(false);
        document.setDescription(
                ValidationUtil.clean(
                        request.getParameter("description")
                )
        );
        document.setDestinationDepartmentId(
                parseRequiredId(
                        request.getParameter("departmentId"),
                        "Please select a destination department."
                )
        );
        document.setBossId(
                parseRequiredId(
                        request.getParameter("bossId"),
                        "Please select a boss."
                )
        );

        return document;
    }

    private static Long parseRequiredId(
            String value,
            String errorMessage
    ) {
        if (!ValidationUtil.hasText(value)) {
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static boolean hasUploadedFile(Part part) {
        return part != null
                && part.getSize() > 0
                && ValidationUtil.hasText(
                        part.getSubmittedFileName()
                );
    }

    private static void validateCapturedImages(
            List<Part> capturedImages
    ) {
        for (Part image : capturedImages) {
            String contentType = image.getContentType();

            if (contentType == null
                    || !contentType
                            .toLowerCase(Locale.ROOT)
                            .startsWith("image/")) {

                throw new IllegalArgumentException(
                        "Only image files can be selected in the Choose Images section."
                );
            }
        }
    }
}
