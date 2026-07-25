package com.chekrol.dms.util;

import javax.servlet.http.HttpSession;
import java.util.UUID;

public final class SubmissionGuard {

    private static final String TOKEN_PREFIX =
            SubmissionGuard.class.getName() + ".TOKEN.";

    private static final String PROCESSING_PREFIX =
            SubmissionGuard.class.getName() + ".PROCESSING.";

    private SubmissionGuard() {
        // Utility class
    }

    /**
     * Creates a new token when the form is displayed.
     */
    public static String issueToken(
            HttpSession session,
            String formKey
    ) {
        String token = UUID.randomUUID().toString();

        synchronized (session) {
            session.setAttribute(
                    TOKEN_PREFIX + formKey,
                    token
            );

            session.removeAttribute(
                    PROCESSING_PREFIX + formKey
            );
        }

        return token;
    }

    /**
     * Attempts to start processing.
     *
     * Returns false when:
     * - the token is missing;
     * - the token is invalid;
     * - the request is already being processed;
     * - the same form was already submitted successfully.
     */
    public static boolean begin(
            HttpSession session,
            String formKey,
            String submittedToken
    ) {
        if (!ValidationUtil.hasText(submittedToken)) {
            return false;
        }

        synchronized (session) {
            String expectedToken = (String) session.getAttribute(
                    TOKEN_PREFIX + formKey
            );

            String processingToken = (String) session.getAttribute(
                    PROCESSING_PREFIX + formKey
            );

            if (!submittedToken.equals(expectedToken)) {
                return false;
            }

            if (submittedToken.equals(processingToken)) {
                return false;
            }

            session.setAttribute(
                    PROCESSING_PREFIX + formKey,
                    submittedToken
            );

            return true;
        }
    }

    /**
     * Invalidates the token after a successful operation.
     */
    public static void complete(
            HttpSession session,
            String formKey
    ) {
        synchronized (session) {
            session.removeAttribute(
                    TOKEN_PREFIX + formKey
            );

            session.removeAttribute(
                    PROCESSING_PREFIX + formKey
            );
        }
    }

    /**
     * Allows a retry when processing failed.
     */
    public static void fail(
            HttpSession session,
            String formKey
    ) {
        synchronized (session) {
            session.removeAttribute(
                    PROCESSING_PREFIX + formKey
            );
        }
    }
}