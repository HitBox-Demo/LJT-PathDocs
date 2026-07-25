package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DepartmentDAO;
import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.dao.UserDAO;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.service.DocumentService;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import com.chekrol.dms.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/documents/edit")
@MultipartConfig(maxFileSize = 50L * 1024 * 1024, maxRequestSize = 150L * 1024 * 1024)
public class DocumentEditServlet extends HttpServlet {
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DocumentService documentService = new DocumentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long documentId = Long.parseLong(request.getParameter("id"));
            User currentUser = (User) request.getSession().getAttribute("currentUser");
            DocumentRecord document = loadEditableDocument(currentUser, documentId);
            showForm(request, response, currentUser, document);
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID.");
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, exception.getMessage());
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long documentId = 0L;
        User currentUser = (User) request.getSession().getAttribute("currentUser");

        try {
            documentId = Long.parseLong(request.getParameter("documentId"));
            DocumentRecord existing = loadEditableDocument(currentUser, documentId);
            DocumentRecord update = readForm(request, existing);
            String action = normaliseAction(request.getParameter("action"));

            Collection<Part> allParts = request.getParts();
            Collection<Part> imageParts = allParts.stream()
                    .filter(part -> "captureImages".equals(part.getName()))
                    .filter(DocumentEditServlet::hasUploadedFile)
                    .collect(Collectors.toList());
            Part primaryPdf = allParts.stream()
                    .filter(part -> "primaryPdf".equals(part.getName()))
                    .filter(DocumentEditServlet::hasUploadedFile)
                    .findFirst()
                    .orElse(null);
            Collection<Part> attachments = allParts.stream()
                    .filter(part -> "attachments".equals(part.getName()))
                    .filter(DocumentEditServlet::hasUploadedFile)
                    .collect(Collectors.toList());
            Set<Long> removeFileIds = parseIds(request.getParameterValues("removeFileId"));

            documentService.update(
                    currentUser,
                    update,
                    imageParts,
                    primaryPdf,
                    attachments,
                    removeFileIds,
                    action
            );

            String message = switch (action) {
                case "resubmit" -> "Document corrected and resubmitted for approval.";
                case "draft" -> "Document saved as draft.";
                default -> "Document corrections saved.";
            };
            request.getSession().setAttribute("flashSuccess", message);
            response.sendRedirect(request.getContextPath() + "/documents/view?id=" + documentId);
        } catch (Exception exception) {
            request.setAttribute("error", exception.getMessage());
            try {
                DocumentRecord existing = loadEditableDocument(currentUser, documentId);
                DocumentRecord formDocument = readFormSafely(request, existing);
                showForm(request, response, currentUser, formDocument);
            } catch (Exception secondaryException) {
                throw new ServletException(exception);
            }
        }
    }

    private DocumentRecord loadEditableDocument(User user, long documentId) throws Exception {
        DocumentRecord document = AppConfig.isDemoMode()
                ? DemoData.findDocument(user, documentId)
                : documentDAO.findAuthorized(user, documentId);

        if (document == null) {
            throw new IllegalArgumentException("Document not found or access denied.");
        }
        if (!Long.valueOf(user.getId()).equals(document.getCreatedById())) {
            throw new IllegalArgumentException("Only the clerk who created this document can edit it.");
        }
        if (!isEditableStatus(document.getStatus())) {
            throw new IllegalArgumentException("Only returned, recalled or draft documents can be edited.");
        }
        return document;
    }

    private boolean isEditableStatus(String status) {
        return "RETURNED_FOR_CORRECTION".equals(status)
                || "DRAFT".equals(status)
                || "RECALLED".equals(status);
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User currentUser,
            DocumentRecord document
    ) throws Exception {
        request.setAttribute("document", document);
        request.setAttribute(
                "departments",
                AppConfig.isDemoMode() ? DemoData.departments() : departmentDAO.listActive()
        );
        request.setAttribute(
                "bosses",
                AppConfig.isDemoMode() ? DemoData.bosses() : userDAO.listBosses()
        );
        request.setAttribute(
                "files",
                AppConfig.isDemoMode()
                        ? DemoData.filesForDocument(currentUser, document.getId())
                        : documentDAO.listFiles(document.getId())
        );
        request.setAttribute("pageTitle", "Correct " + document.getDocumentCode());
        request.getRequestDispatcher("/WEB-INF/views/documents/edit.jsp").forward(request, response);
    }

    private DocumentRecord readForm(HttpServletRequest request, DocumentRecord existing) {
        String title = ValidationUtil.clean(request.getParameter("title"));
        String reference = ValidationUtil.clean(request.getParameter("referenceNo"));
        String sender = ValidationUtil.clean(request.getParameter("sender"));
        String dateReceived = ValidationUtil.clean(request.getParameter("dateReceived"));
        String category = ValidationUtil.clean(request.getParameter("category"));
        String description = ValidationUtil.clean(request.getParameter("description"));
        String departmentId = ValidationUtil.clean(request.getParameter("departmentId"));
        String bossId = ValidationUtil.clean(request.getParameter("bossId"));

        if (!ValidationUtil.hasText(title)
                || !ValidationUtil.hasText(reference)
                || !ValidationUtil.hasText(sender)
                || !ValidationUtil.hasText(dateReceived)
                || !ValidationUtil.hasText(category)
                || !ValidationUtil.hasText(departmentId)
                || !ValidationUtil.hasText(bossId)) {
            throw new IllegalArgumentException("Complete all required document and routing fields.");
        }

        DocumentRecord update = new DocumentRecord();
        update.setId(existing.getId());
        update.setDocumentCode(existing.getDocumentCode());
        update.setCreatedById(existing.getCreatedById());
        update.setCreatedByName(existing.getCreatedByName());
        update.setCreatedAt(existing.getCreatedAt());
        update.setStatus(existing.getStatus());
        update.setRejectionReason(existing.getRejectionReason());
        update.setTitle(title);
        update.setReferenceNo(reference);
        update.setSender(sender);
        update.setDateReceived(LocalDate.parse(dateReceived));
        update.setCategory(category);
        update.setPriority("URGENT".equals(request.getParameter("priority")) ? "URGENT" : "NORMAL");
        update.setDescription(description);
        update.setDestinationDepartmentId(Long.valueOf(departmentId));
        update.setBossId(Long.valueOf(bossId));
        return update;
    }

    private DocumentRecord readFormSafely(HttpServletRequest request, DocumentRecord existing) {
        try {
            return readForm(request, existing);
        } catch (Exception ignored) {
            return existing;
        }
    }

    private String normaliseAction(String action) {
        return switch (action == null ? "" : action) {
            case "save", "draft", "resubmit" -> action;
            default -> throw new IllegalArgumentException("Unsupported document edit action.");
        };
    }

    private Set<Long> parseIds(String[] values) {
        if (values == null) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    private static boolean hasUploadedFile(Part part) {
        return part != null
                && part.getSize() > 0
                && ValidationUtil.hasText(part.getSubmittedFileName());
    }

}
