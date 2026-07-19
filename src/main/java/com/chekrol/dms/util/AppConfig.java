package com.chekrol.dms.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {
    private AppConfig() {}

    public static boolean isDemoMode() {
        return Boolean.parseBoolean(value("DMS_DEMO_MODE", "true"));
    }

    public static Path storageRoot() {
        String fallback = Paths.get(System.getProperty("user.home"), "ljtrouteflow-storage").toString();
        return Paths.get(value("DMS_STORAGE_ROOT", fallback)).toAbsolutePath().normalize();
    }

    public static long maxFileBytes() {
        long mb;
        try { mb = Long.parseLong(value("DMS_MAX_FILE_MB", "20")); }
        catch (NumberFormatException ex) { mb = 20; }
        return Math.max(1, mb) * 1024L * 1024L;
    }

    public static String value(String name, String fallback) {
        String system = System.getProperty(name);
        if (system != null && !system.isBlank()) return system.trim();
        String env = System.getenv(name);
        if (env != null && !env.isBlank()) return env.trim();
        return fallback;
    }
}
