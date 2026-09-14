package com.chekrol.dms.controller;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import com.chekrol.dms.util.FileStorageUtil;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

@WebServlet("/documents/thumbnail")
public class DocumentThumbnailServlet extends HttpServlet {

    private static final int MAX_WIDTH = 820;
    private static final int MAX_HEIGHT = 1080;
    private static final float PDF_DPI = 105.0f;

    private final DocumentDAO documentDAO = new DocumentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {

        try {
            long fileId = Long.parseLong(
                    request.getParameter("fileId")
            );

            User currentUser = (User) request
                    .getSession()
                    .getAttribute("currentUser");

            DocumentFile file = AppConfig.isDemoMode()
                    ? DemoData.findFileAuthorized(
                            currentUser,
                            fileId
                    )
                    : documentDAO.findFileAuthorized(
                            currentUser,
                            fileId
                    );

            if (file == null || !file.isPrimaryFile()) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
                return;
            }

            Path path = FileStorageUtil.resolveAuthorized(
                    file.getStoragePath()
            );

            BufferedImage source = renderFrontPage(path, file);
            BufferedImage scaled = scaleDown(source);
            BufferedImage blurred = applyPrivacyBlur(scaled);

            response.setHeader(
                    "Cache-Control",
                    "no-store, no-cache, must-revalidate, private, max-age=0"
            );
            response.setHeader("Pragma", "no-cache");
            response.setHeader(
                    "X-Content-Type-Options",
                    "nosniff"
            );
            response.setContentType("image/png");

            if (!ImageIO.write(
                    blurred,
                    "png",
                    response.getOutputStream()
            )) {
                throw new IOException(
                        "PNG thumbnail writer is unavailable."
                );
            }

        } catch (NumberFormatException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid file ID."
            );

        } catch (UnsupportedPreviewException exception) {
            response.sendError(
                    HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    exception.getMessage()
            );

        } catch (Exception exception) {
            throw new ServletException(
                    "Unable to generate the document thumbnail.",
                    exception
            );
        }
    }

    private BufferedImage renderFrontPage(
            Path path,
            DocumentFile file
    ) throws IOException, UnsupportedPreviewException {

        String mimeType = file.getMimeType() == null
                ? ""
                : file.getMimeType()
                        .toLowerCase(Locale.ROOT);

        String fileName = file.getOriginalName() == null
                ? ""
                : file.getOriginalName()
                        .toLowerCase(Locale.ROOT);

        if ("application/pdf".equals(mimeType)
                || fileName.endsWith(".pdf")) {

            try (PDDocument document = Loader.loadPDF(
                    path.toFile()
            )) {
                if (document.getNumberOfPages() < 1) {
                    throw new UnsupportedPreviewException(
                            "The PDF has no pages."
                    );
                }

                PDFRenderer renderer = new PDFRenderer(document);

                return renderer.renderImageWithDPI(
                        0,
                        PDF_DPI,
                        ImageType.RGB
                );
            }
        }

        if (mimeType.startsWith("image/")) {
            BufferedImage image = ImageIO.read(path.toFile());

            if (image == null) {
                throw new UnsupportedPreviewException(
                        "The image preview could not be read."
                );
            }

            return image;
        }

        throw new UnsupportedPreviewException(
                "A thumbnail is available only for PDF or image primary files."
        );
    }

    private BufferedImage scaleDown(BufferedImage source) {
        double widthRatio =
                (double) MAX_WIDTH / source.getWidth();

        double heightRatio =
                (double) MAX_HEIGHT / source.getHeight();

        double ratio = Math.min(
                1.0,
                Math.min(widthRatio, heightRatio)
        );

        if (ratio >= 1.0) {
            return copyAsRgb(source);
        }

        int targetWidth = Math.max(
                1,
                (int) Math.round(source.getWidth() * ratio)
        );

        int targetHeight = Math.max(
                1,
                (int) Math.round(source.getHeight() * ratio)
        );

        BufferedImage scaled = new BufferedImage(
                targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = scaled.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );
            graphics.drawImage(
                    source,
                    0,
                    0,
                    targetWidth,
                    targetHeight,
                    null
            );

        } finally {
            graphics.dispose();
        }

        return scaled;
    }

    private BufferedImage copyAsRgb(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = copy.createGraphics();

        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return copy;
    }

    /**
     * The blur is applied on the server. The browser never receives an
     * unblurred first-page image from this thumbnail endpoint.
     */
    private BufferedImage applyPrivacyBlur(BufferedImage source) {
        int kernelSize = 11;
        float weight = 1.0f / (kernelSize * kernelSize);
        float[] kernelData =
                new float[kernelSize * kernelSize];

        for (int index = 0;
             index < kernelData.length;
             index++) {
            kernelData[index] = weight;
        }

        ConvolveOp blur = new ConvolveOp(
                new Kernel(
                        kernelSize,
                        kernelSize,
                        kernelData
                ),
                ConvolveOp.EDGE_NO_OP,
                null
        );

        BufferedImage firstPass = blur.filter(source, null);
        BufferedImage secondPass = blur.filter(firstPass, null);

        Graphics2D graphics = secondPass.createGraphics();

        try {
            graphics.setComposite(
                    AlphaComposite.SrcOver.derive(0.16f)
            );
            graphics.setColor(java.awt.Color.WHITE);
            graphics.fillRect(
                    0,
                    0,
                    secondPass.getWidth(),
                    secondPass.getHeight()
            );
        } finally {
            graphics.dispose();
        }

        return secondPass;
    }

    private static final class UnsupportedPreviewException
            extends Exception {

        private UnsupportedPreviewException(String message) {
            super(message);
        }
    }
}
