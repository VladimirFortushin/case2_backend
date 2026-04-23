package ru.mephi.case2.db;

import ru.mephi.case2.db.config.Postgresql;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.log.BackendLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbVideoRepository implements  VideoRepository {

    private final String queryOut = "bot_backend - [SQL]: OUT ";
    private final String queryIn = "bot_backend - [SQL]: IN ";
    private final String queryErr = "bot_backend - [SQL]: ERROR ";

    private final String getUrlsWithPlatformsQuery = """
            select u.url, p.name AS platform_name
            from urls u
            join platforms p ON u.platform_id = p.id
        """;

    @Override
    public Map<String, Platform> getUrlsWithPlatforms() {
        Map<String, Platform> result = new HashMap<>();
        BackendLogger.log(queryOut + getUrlsWithPlatformsQuery);
        try (Connection conn = Postgresql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getUrlsWithPlatformsQuery);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String url = rs.getString("url");
                String platformName = rs.getString("platform_name");
                Platform platform = mapDbPlatformNameToEnum(platformName);
                result.put(url, platform);
            }
            if(!result.isEmpty()) {
                BackendLogger.log(queryIn + result);
            }
        } catch (SQLException e) {
            BackendLogger.log(queryErr + "Failed to fetch URLs with platforms: " + e.getMessage());
            throw new RuntimeException("Database error in getUrlsWithPlatforms", e);
        }

        return result;

    }

    private Platform mapDbPlatformNameToEnum(String platformName) {
        return switch (platformName) {
            case "youtube" -> Platform.YOUTUBE;
            case "vimeo" -> Platform.VIMEO;
            case "rutube" -> Platform.RUTUBE;
            default -> Platform.UNKNOWN;
        };
    }
}
