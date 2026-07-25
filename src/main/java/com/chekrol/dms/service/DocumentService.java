package com.chekrol.dms.service;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import com.chekrol.dms.util.FileStorageUtil;
import com.chekrol.dms.util.ImageToPdfService;
import com.chekrol.dms.util.ValidationUtil;

import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DocumentService {
    private final DocumentDAO documentDAO = new DocumentDAO();

    public long create(
            User user,
            DocumentRecord document,
            Collection<Part> imageParts,
            Part primaryPdf,
            Collection<Part> attachments,
            boolean submit
    ) throws IOException, SQLException {
        document.setCreatedById(user.getId());
        document.setDocumentCode(generateDocumentCode());
        document.setDueDate(submit ? calculateDueDate(document.getPriority()) : null);

        List<DocumentFile> files = new ArrayList<>();
        List<Path> storedPaths = new ArrayList<>();
        try {
            files.addAll(createPrimaryFiles(document.getDocumentCode(), imageParts, primaryPdf, true));
            files.addAll(storeAttachments(document.getDocumentCode(), attachments));
            files.stream()
                    .map(DocumentFile::getStoragePath)
                    .filter(path -> path != null && !path.isBlank())
                    .map(Path::of)
                    .forEach(storedPaths::add);

            if (AppConfig.isDemoMode()) {
                DemoData.createDocument(user, document, files, submit);
                return document.getId();
            }
            return documentDAO.create(document, files, submit);
        } catch (IOException | SQLException | RuntimeException exception) {
            deleteQuietly(storedPaths);
            throw exception;
        }
    }

    public void update(
            User user,
            DocumentRecord document,
            Collection<Part> imageParts,
            Part primaryPdf,
            Collection<Part> attachments,
            Set<Long> removeFileIds,
            String action
    ) throws IOException, SQLException {
        List<DocumentFile> existingFiles = AppConfig.isDemoMode()
                ? DemoData.filesForDocument(user, document.getId())
                : documentDAO.listFiles(document.getId());

        List<DocumentFile> newFiles = new ArrayList<>();
        List<Path> newlyStoredPaths = new ArrayList<>();

        try {
            newFiles.addAll(createPrimaryFiles(
                    document.getDocumentCode(), imageParts, primaryPdf, false
            ));
            newFiles.addAll(storeAttachments(document.getDocumentCode(), attachments));
            newFiles.stream()
                    .map(DocumentFile::getStoragePath)
                    .filter(path -> path != null && !path.isBlank())
                    .map(Path::of)
                    .forEach(newlyStoredPaths::add);

            boolean replacingPrimary = newFiles.stream().anyMatch(DocumentFile::isPrimaryFile);
            Set<Long> safeRemoveIds = removeFileIds == null ? Set.of() : new HashSet<>(removeFileIds);

            if (AppConfig.isDemoMode()) {
                DemoData.updateDocument(user, document, newFiles, safeRemoveIds, action);
            } else {
                documentDAO.update(user, document, newFiles, safeRemoveIds, replacingPrimary, action);
            }

            deleteReplacedFiles(existingFiles, safeRemoveIds, replacingPrimary);
        } catch (IOException | SQLException | RuntimeException exception) {
            deleteQuietly(newlyStoredPaths);
            throw exception;
        }
    }

    private List<DocumentFile> createPrimaryFiles(
            String documentCode,
            Collection<Part> imageParts,
            Part primaryPdf,
            boolean primaryRequired
    ) throws IOException {
        List<Path> temporaryImages = new ArrayList<>();
        try {
            if (imageParts != null) {
                for (Part part : imageParts) {
                    if (!hasFile(part)) {
                        continue;
                    }
                    if (!ValidationUtil.isImage(part)) {
                        throw new IOException("Selected pages must be JPG, JPEG or PNG images.");
                    }
                    FileStorageUtil.StoredFile stored = FileStorageUtil.store(
                            part, documentCode, "temporary"
                    );
                    temporaryImages.add(stored.getPath());
                }
            }

            if (!temporaryImages.isEmpty()) {
                Path output = AppConfig.storageRoot()
                        .resolve("documents")
                        .resolve(documentCode)
                        .resolve("main")
                        .resolve("captured-document-" + UUID.randomUUID() + ".pdf");
                ImageToPdfService.convert(temporaryImages, output);

                DocumentFile file = new DocumentFile();
                file.setOriginalName("captured-document.pdf");
                file.setStorageName(output.getFileName().toString());
                file.setStoragePath(output.toString());
                file.setMimeType("application/pdf");
                file.setFileSize(Files.size(output));
                file.setPrimaryFile(true);
                return List.of(file);
            }

            if (hasFile(primaryPdf)) {
                if (!"pdf".equals(ValidationUtil.extension(primaryPdf.getSubmittedFileName()))) {
                    throw new IOException("The main uploaded document must be a PDF.");
                }
                FileStorageUtil.StoredFile stored = FileStorageUtil.store(
                        primaryPdf, documentCode, "main"
                );
                return List.of(toFile(stored, true));
            }

            if (primaryRequired) {
                throw new IOException("Choose at least one image or upload a main PDF document.");
            }
            return List.of();
        } finally {
            deleteQuietly(temporaryImages);
        }
    }

    private List<DocumentFile> storeAttachments(
            String documentCode,
            Collection<Part> attachments
    ) throws IOException {
        List<DocumentFile> files = new ArrayList<>();
        if (attachments == null) {
            return files;
        }
        for (Part part : attachments) {
            if (!hasFile(part)) {
                continue;
            }
            FileStorageUtil.StoredFile stored = FileStorageUtil.store(
                    part, documentCode, "attachments"
            );
            files.add(toFile(stored, false));
        }
        return files;
    }

    private void deleteReplacedFiles(
            List<DocumentFile> existingFiles,
            Set<Long> removeFileIds,
            boolean replacingPrimary
    ) {
        for (DocumentFile file : existingFiles) {
            boolean shouldDelete = (replacingPrimary && file.isPrimaryFile())
                    || (!file.isPrimaryFile() && removeFileIds.contains(file.getId()));
            if (!shouldDelete || file.getStoragePath() == null) {
                continue;
            }
            try {
                Files.deleteIfExists(Path.of(file.getStoragePath()));
            } catch (IOException ignored) {
                // Database metadata is already committed. Old-file cleanup is best effort.
            }
        }
    }

    private DocumentFile toFile(FileStorageUtil.StoredFile stored, boolean primary) {
        DocumentFile file = new DocumentFile();
        file.setOriginalName(stored.getOriginalName());
        file.setStorageName(stored.getStorageName());
        file.setStoragePath(stored.getPath().toString());
        file.setMimeType(stored.getMimeType());
        file.setFileSize(stored.getSize());
        file.setPrimaryFile(primary);
        return file;
    }

    private boolean hasFile(Part part) {
        return part != null
                && part.getSize() > 0
                && ValidationUtil.hasText(part.getSubmittedFileName());
    }

    private LocalDate calculateDueDate(String priority) {
        return LocalDate.now().plusDays("URGENT".equals(priority) ? 2 : 5);
    }

    private String generateDocumentCode() {
        return "DOC-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void deleteQuietly(Collection<Path> paths) {
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Cleanup failure must not hide the original business error.
            }
        }
    }
}
