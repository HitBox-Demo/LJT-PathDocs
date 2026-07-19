package com.chekrol.dms.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class FileStorageUtil {
    private FileStorageUtil() {}

    public static StoredFile store(Part part, String documentCode, String folder) throws IOException {
        if (!ValidationUtil.isAllowedUpload(part)) {
            throw new IOException("Unsupported file type or file exceeds the configured limit.");
        }
        ValidationUtil.validateSignature(part);
        String original = ValidationUtil.safeOriginalName(part.getSubmittedFileName());
        String extension = ValidationUtil.extension(original);
        String storageName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        Path root = AppConfig.storageRoot();
        Path directory = root.resolve("documents").resolve(documentCode).resolve(folder).normalize();
        if (!directory.startsWith(root)) throw new IOException("Invalid storage path.");
        Files.createDirectories(directory);
        Path target = directory.resolve(storageName).normalize();
        if (!target.startsWith(directory)) throw new IOException("Invalid storage path.");
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return new StoredFile(original, storageName, target, part.getContentType(), part.getSize());
    }

    public static Path resolveAuthorized(String storedPath) throws IOException {
        Path root = AppConfig.storageRoot();
        Path path = Path.of(storedPath).toAbsolutePath().normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) throw new IOException("File not found or path is not authorised.");
        return path;
    }

    public static final class StoredFile {
        private final String originalName;
        private final String storageName;
        private final Path path;
        private final String mimeType;
        private final long size;

        public StoredFile(String originalName, String storageName, Path path, String mimeType, long size) {
            this.originalName = originalName; this.storageName = storageName; this.path = path; this.mimeType = mimeType; this.size = size;
        }
        public String getOriginalName() { return originalName; }
        public String getStorageName() { return storageName; }
        public Path getPath() { return path; }
        public String getMimeType() { return mimeType; }
        public long getSize() { return size; }
    }
}
