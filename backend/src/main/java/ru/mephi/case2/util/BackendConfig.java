package ru.mephi.case2.util;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.log.BackendLogger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BackendConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = BackendConfig.class
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

    public static int getStatsUpdateFrequency(){
        int delaySec = 0;
        try{
            delaySec = Integer.parseInt(envOrProp("STATS_UPDATE_DELAY_SEC", "stats.update.delay"));
        }catch (Exception ignored){

        }
        return delaySec == 0 ? 60 : delaySec;
    }

    private static String envOrProp(String envName, String propName) {
        String envValue = System.getenv(envName);
        return (envValue != null && !envValue.isBlank()) ? envValue : props.getProperty(propName, "");
    }

    public static void validateParams() {
        List<String> missing = new ArrayList<>();

        checkNotEmpty("DB_URL", getDbUrl(), missing);
        checkNotEmpty("DB_USERNAME", getDbUserName(), missing);
        checkNotEmpty("DB_PASSWORD", getDbPassword(), missing);

        checkNotEmpty("YOUTUBE_API_TOKEN", getApiToken(Platform.YOUTUBE), missing);
        checkNotEmpty("YOUTUBE_API_URL", getApiUrl(Platform.YOUTUBE), missing);

//         checkNotEmpty("RUTUBE_API_TOKEN", getApiToken(Platform.RUTUBE), missing);
         checkNotEmpty("RUTUBE_API_URL", getApiUrl(Platform.RUTUBE), missing);

         checkNotEmpty("VIMEO_API_TOKEN", getApiToken(Platform.VIMEO), missing);
         checkNotEmpty("VIMEO_API_URL", getApiUrl(Platform.VIMEO), missing);

        if (!missing.isEmpty()) {
            String message = "Missing required configuration parameters:\n" + String.join("\n", missing);
            BackendLogger.log("FATAL: " + message);
            throw new IllegalStateException(message);
        }
    }

    private static void checkNotEmpty(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add("  - " + name + " (env: " + name + " or property in application.properties)");
        }
    }
}