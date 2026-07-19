package com.chekrol.dms.util;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

        IOException exception = assertThrows(IOException.class,
                () -> ImageToPdfService.convert(Collections.emptyList(), output));

        assertEquals("At least one image is required.", exception.getMessage());
    }

    @Test
    void convertShouldCreatePdfFromImage() throws IOException {
        Path tempDir = Files.createTempDirectory("image-to-pdf-test");
        Path image = tempDir.resolve("sample.png");
        BufferedImage bufferedImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 20, 20);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);
        Files.write(image, outputStream.toByteArray());
        Path output = tempDir.resolve("out").resolve("document.pdf");

        Path result = ImageToPdfService.convert(List.of(image), output);

        assertEquals(output, result);
        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0L);
    }
}
