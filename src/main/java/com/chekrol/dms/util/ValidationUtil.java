package com.chekrol.dms.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Pattern SAFE_TEXT = Pattern.compile("^[\\p{L}\\p{N} .,'&()/_\\-:]{1,250}$");

    private ValidationUtil() {}

    public static boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
    public static String clean(String value) { return value == null ? "" : value.trim(); }
    public static boolean isReasonableText(String value) { return value == null || value.isBlank() || SAFE_TEXT.matcher(value.trim()).matches(); }
    public static String extension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
    public static boolean isAllowedUpload(Part part) {
        if (part == null || part.getSize() <= 0 || part.getSize() > AppConfig.maxFileBytes()) return false;
        return ALLOWED_EXTENSIONS.contains(extension(part.getSubmittedFileName()));
    }
    public static boolean isImage(Part part) {
        return part != null && IMAGE_EXTENSIONS.contains(extension(part.getSubmittedFileName()));
    }
    public static void validateSignature(Part part) throws IOException {
        String ext = extension(part.getSubmittedFileName());
        byte[] header = new byte[8];
        int read;
        try (InputStream input = part.getInputStream()) { read = input.read(header); }
        if (read < 4) throw new IOException("Uploaded file is empty or has an invalid signature.");
        boolean ok = switch (ext) {
            case "pdf" -> header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
            case "jpg", "jpeg" -> (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
            case "png" -> Arrays.equals(Arrays.copyOf(header, 8), new byte[]{(byte)0x89, 'P','N','G',0x0d,0x0a,0x1a,0x0a});
            case "docx", "xlsx" -> header[0] == 'P' && header[1] == 'K';
            case "doc", "xls" -> (header[0] & 0xff) == 0xd0 && (header[1] & 0xff) == 0xcf && (header[2] & 0xff) == 0x11 && (header[3] & 0xff) == 0xe0;
            default -> false;
        };
        if (!ok) throw new IOException("The uploaded file content does not match its extension: " + ext);
    }

    public static String safeOriginalName(String filename) {
        String name = filename == null ? "file" : filename.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._ -]", "_");
        return name.isBlank() ? "file" : name;
    }
}
