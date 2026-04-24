package ru.mephi.case2.db;

import ru.mephi.case2.db.config.Postgresql;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.db.entity.UrlInfo;
import ru.mephi.case2.log.BackendLogger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbVideoRepository implements VideoRepository {

    private final String queryOut = "bot_backend - [SQL]: OUT ";
    private final String queryIn  = "bot_backend - [SQL]: IN ";
    private final String queryErr = "bot_backend - [SQL]: ERROR ";



    //получение урл с id
    public List<UrlInfo> getUrlsWithPlatforms() {
        String sql = """
            SELECT u.id, u.url, p.name AS platform_name
            FROM urls u
            JOIN platforms p ON u.platform_id = p.id
        """;
        List<UrlInfo> result = new ArrayList<>();
        BackendLogger.log(queryOut + sql);

        try (Connection conn = Postgresql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String url = rs.getString("url");
                Platform platform = mapDbPlatformNameToEnum(rs.getString("platform_name"));
                result.add(new UrlInfo(id, url, platform));
            }
            if (!result.isEmpty()) {
                BackendLogger.log(queryIn + "Loaded " + result.size() + " URLs");
            }
        } catch (SQLException e) {
            BackendLogger.log(queryErr + "Failed to fetch URLs with platforms: " + e.getMessage());
            throw new RuntimeException("Database error in getUrlsWithPlatforms", e);
        }
        return result;
    }

    //вставка новой статы в video_stats
    public void saveVideoStats(Map<String, Integer> urlViewsMap,
                               Map<String, Long> urlToIdMap) {
        String sql = "INSERT INTO video_stats (url_id, stats, status, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = Postgresql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int inserted = 0;
            for (var entry : urlViewsMap.entrySet()) {
                String url = entry.getKey();
                Integer views = entry.getValue();
                Long urlId = urlToIdMap.get(url);
                if (urlId == null) {
                    BackendLogger.log(queryErr + "No id for URL: " + url);
                    continue;
                }
                boolean success = views != null && views >= 0;
                stmt.setLong(1, urlId);
                stmt.setInt(2, success ? views : 0);
                stmt.setBoolean(3, success);
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.addBatch();
                inserted++;
            }
            if (inserted > 0) {
                stmt.executeBatch();
                BackendLogger.log(queryIn + "Inserted " + inserted + " stats records");
            }
        } catch (SQLException e) {
            BackendLogger.log(queryErr + "Failed to save video stats: " + e.getMessage());
        }
    }

    private Platform mapDbPlatformNameToEnum(String platformName) {
        return switch (platformName) {
            case "youtube" -> Platform.YOUTUBE;
            case "vimeo"   -> Platform.VIMEO;
            case "rutube"  -> Platform.RUTUBE;
            default        -> Platform.UNKNOWN;
        };
    }
}