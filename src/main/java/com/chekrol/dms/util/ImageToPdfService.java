package com.chekrol.dms.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ImageToPdfService {
    private ImageToPdfService() {}

    public static Path convert(List<Path> images, Path output) throws IOException {
        if (images == null || images.isEmpty()) throw new IOException("At least one image is required.");
        Files.createDirectories(output.getParent());
        try (PDDocument pdf = new PDDocument()) {
            for (Path imagePath : images) {
                byte[] bytes = Files.readAllBytes(imagePath);
                PDImageXObject image = PDImageXObject.createFromByteArray(pdf, bytes, imagePath.getFileName().toString());
                boolean landscape = image.getWidth() > image.getHeight();
                float pageWidth = landscape ? PDRectangle.A4.getHeight() : PDRectangle.A4.getWidth();
                float pageHeight = landscape ? PDRectangle.A4.getWidth() : PDRectangle.A4.getHeight();
                PDRectangle pageSize = new PDRectangle(pageWidth, pageHeight);
                PDPage page = new PDPage(pageSize);
                pdf.addPage(page);

                float margin = 28f;
                float availableWidth = pageSize.getWidth() - margin * 2;
                float availableHeight = pageSize.getHeight() - margin * 2;
                float scale = Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight());
                float width = image.getWidth() * scale;
                float height = image.getHeight() * scale;
                float x = (pageSize.getWidth() - width) / 2;
                float y = (pageSize.getHeight() - height) / 2;

                try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
                    content.drawImage(image, x, y, width, height);
                }
            }
            pdf.save(output.toFile());
        }
        return output;
    }
}
