package ru.mephi.case2.util;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.log.BackendLogger;

import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ApiConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            BackendLogger.log("Cannot load application.properties config");
            throw new RuntimeException("Cannot load config", e);
        }
    }

    public static String getApiUrl(Platform platform) {
        return switch (platform) {
            case YOUTUBE -> envOrProp("YOUTUBE_API_URL", "api.youtube.url");
            case RUTUBE -> envOrProp("RUTUBE_API_URL", "api.rutube.url");
            case VIMEO -> envOrProp("VIMEO_API_URL", "api.vimeo.url");
            default -> throw new IllegalArgumentException("Unknown platform");
        };
    }

    public static String getApiToken(Platform platform) {
        return switch (platform) {
            case YOUTUBE -> envOrProp("YOUTUBE_API_TOKEN", "api.youtube.token");
            case RUTUBE -> envOrProp("RUTUBE_API_TOKEN", "api.rutube.token");
            case VIMEO -> envOrProp("VIMEO_API_TOKEN", "api.vimeo.token");
            default -> throw new IllegalArgumentException("Unknown platform");
        };
    }

    public static String getDbUrl() {
        return envOrProp("DB_URL", "db.url");
    }

    public static String getDbUserName() {
        return envOrProp("DB_USERNAME", "db.username");
    }

    public static String getDbPassword() {
        return envOrProp("DB_PASSWORD", "db.password");
    }

    private static String envOrProp(String envName, String propName) {
        String envValue = System.getenv(envName);
        return (envValue != null && !envValue.isBlank()) ? envValue : props.getProperty(propName, "");
    }
}