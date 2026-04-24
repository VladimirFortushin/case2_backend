package ru.mephi.case2.util;

import ru.mephi.case2.db.entity.Platform;

import java.util.Properties;

import java.io.InputStream;

public class ApiConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ApiConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            props.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Cannot load config", e);
        }
    }

    public static String getApiUrl(Platform platform) {
        return switch (platform) {
            case YOUTUBE -> props.getProperty("api.youtube.url");
            case RUTUBE -> props.getProperty("api.rutube.url");
            case VIMEO -> props.getProperty("api.vimeo.url");
            default -> throw new IllegalArgumentException("Unknown platform");
        };
    }

    public static String getApiToken(Platform platform) {
        return switch (platform) {
            case YOUTUBE -> props.getProperty("api.youtube.token");
            case RUTUBE -> props.getProperty("api.rutube.token");
            case VIMEO -> props.getProperty("api.vimeo.token");
            default -> throw new IllegalArgumentException("Unknown platform");
        };
    }
}
