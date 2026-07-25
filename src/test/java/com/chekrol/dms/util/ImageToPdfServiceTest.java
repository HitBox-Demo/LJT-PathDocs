package com.chekrol.dms.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageToPdfServiceTest {
    @Test
    void convertShouldThrowWhenImagesAreMissing() throws IOException {
        Path output = Files.createTempFile("image-to-pdf", ".pdf");

        IOException exception = assertThrows(
                IOException.class,
                () -> ImageToPdfService.convert(Collections.emptyList(), output)
        );

        assertEquals("At least one image is required.", exception.getMessage());
    }

    @Test
    void convertShouldCreateOnePdfPageForEverySelectedImage() throws IOException {
        System.setProperty("java.awt.headless", "true");
        Path directory = Files.createTempDirectory("image-to-pdf-test");
        Path firstImage = directory.resolve("first.png");
        Path secondImage = directory.resolve("second.png");
        writeImage(firstImage, Color.WHITE);
        writeImage(secondImage, Color.LIGHT_GRAY);
        Path output = directory.resolve("out").resolve("document.pdf");

        Path result = ImageToPdfService.convert(List.of(firstImage, secondImage), output);

        assertEquals(output, result);
        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0L);
        try (PDDocument document = Loader.loadPDF(output.toFile())) {
            assertEquals(2, document.getNumberOfPages());
        }
    }

    private void writeImage(Path path, Color color) throws IOException {
        BufferedImage image = new BufferedImage(20, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }
        ImageIO.write(image, "png", path.toFile());
    }
}
