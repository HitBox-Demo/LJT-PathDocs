package com.chekrol.dms.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    private static final String CLERK_SEED_HASH =
            "pbkdf2_sha256$210000$Y2xlcmstc2VlZC1zYWx0MQ==$"
                    + "FS86yqtytEpR6EpYelLrAIiKmcWu0P21Set68J5Zbms=";
    private static final String BOSS_SEED_HASH =
            "pbkdf2_sha256$210000$Ym9zcy1zZWVkLXNhbHQxMg==$"
                    + "zpsygi16dNhuyA5fT3u+tODyLHRqLRNb2UCVerBdTz8=";
    private static final String DEPARTMENT_SEED_HASH =
            "pbkdf2_sha256$210000$ZGVwdC1zZWVkLXNhbHQxMg==$"
                    + "5E1PzK3vN4X35u5JvdtO5RiJlrTY0LX3sdwnNHNguqw=";

    @Test
    void hashShouldRejectShortPasswords() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PasswordUtil.hash("short")
        );
        assertTrue(exception.getMessage().contains("8 characters"));
    }

    @Test
    void hashAndVerifyShouldWorkForMatchingPasswords() {
        String stored = PasswordUtil.hash("StrongPass123");
        assertTrue(PasswordUtil.verify("StrongPass123", stored));
        assertFalse(PasswordUtil.verify("WrongPass123", stored));
    }

    @Test
    void verifyShouldReturnFalseForMalformedStoredHash() {
        assertFalse(PasswordUtil.verify("StrongPass123", "not-a-valid-hash"));
    }

    @Test
    void OracleSeedHashesShouldMatchDocumentedDemoPasswords() {
        assertTrue(PasswordUtil.verify("Clerk@123", CLERK_SEED_HASH));
        assertTrue(PasswordUtil.verify("Boss@123", BOSS_SEED_HASH));
        assertTrue(PasswordUtil.verify("Dept@123", DEPARTMENT_SEED_HASH));
    }
}
