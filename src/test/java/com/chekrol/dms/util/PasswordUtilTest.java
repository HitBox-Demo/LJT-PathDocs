package com.chekrol.dms.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    @Test
    void hashShouldRejectShortPasswords() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.hash("short"));

        assertTrue(exception.getMessage().contains("8 characters"));
    }

    @Test
    void hashAndVerifyShouldWorkForMatchingPasswords() {
        String password = "StrongPass123";
        String stored = PasswordUtil.hash(password);

        assertTrue(PasswordUtil.verify(password, stored));
    }

    @Test
    void verifyShouldReturnFalseForWrongPassword() {
        String stored = PasswordUtil.hash("StrongPass123");

        assertFalse(PasswordUtil.verify("WrongPass123", stored));
    }

    @Test
    void verifyShouldReturnFalseForMalformedStoredHash() {
        assertFalse(PasswordUtil.verify("StrongPass123", "not-a-valid-hash"));
    }
}
