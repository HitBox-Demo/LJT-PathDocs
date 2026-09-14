package com.chekrol.dms.util;

import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleIntegrationConfigurationTest {

    @Test
    void documentShouldPreserveSubmissionKey() {
        DocumentRecord document = new DocumentRecord();
        document.setSubmissionKey("submission-123");
        assertEquals("submission-123", document.getSubmissionKey());
    }

    @Test
    void roleValuesShouldBeNormalisedForOracleSessions() {
        User user = new User();
        user.getRoles().add(" clerk ");
        user.getRoles().add("system_admin");

        user.normaliseRoles();

        assertTrue(user.hasRole("CLERK"));
        assertTrue(user.hasRole("SYSTEM_ADMIN"));
        assertTrue(user.canManageDocuments());
    }

    @Test
    void directDatabaseConfigurationShouldTakePriority() {
        String oldUrl = System.getProperty("DMS_DB_URL");
        String oldUser = System.getProperty("DMS_DB_USERNAME");
        try {
            System.setProperty(
                    "DMS_DB_URL",
                    "jdbc:oracle:thin:@//localhost:1521/XEPDB1"
            );
            System.setProperty("DMS_DB_USERNAME", "LJT_ROUTE_FLOW");

            assertEquals(
                    "Environment variables / Java system properties",
                    DatabaseConnection.configurationSource()
            );
            assertTrue(DatabaseConnection.isConfigured());
        } finally {
            restore("DMS_DB_URL", oldUrl);
            restore("DMS_DB_USERNAME", oldUser);
        }
    }

    private void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
