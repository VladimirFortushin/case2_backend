package ru.mephi.case2.db;

import ru.mephi.case2.db.config.Postgresql;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.db.entity.UrlInfo;
import ru.mephi.case2.db.entity.UrlStatsRow;
import ru.mephi.case2.db.entity.UserSummary;
import ru.mephi.case2.log.BackendLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbVideoRepository implements VideoRepository {

    private final String queryOut = "bot_backend - [SQL]: OUT ";
    private final String queryIn = "bot_backend - [SQL]: IN ";
    private final String queryErr = "bot_backend - [SQL]: ERROR ";

    @Override
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

    @Override
    public boolean addUrl(String url, Platform platform, long telegramUserId) {
        String sql = """
            INSERT INTO urls (url, platform_id, telegram_user_id)
            VALUES (?, (SELECT id FROM platforms WHERE name = ?), ?)
            ON CONFLICT (url, telegram_user_id) DO NOTHING
        """;
        try (Connection conn = Postgresql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.setString(2, platform.name().toLowerCase());
            stmt.setLong(3, telegramUserId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            BackendLogger.log(queryErr + "Failed to add url: " + e.getMessage());
            throw new RuntimeException("Database error in addUrl", e);
        }
    }

    @Override
    public List<UrlStatsRow> getUrlStatsForUser(long telegramUserId) {
        String sql = """
            SELECT u.url,
                   p.name as platform_name,
                   latest.status as latest_status,
                   latest.stats as latest_stats,
                   (
                     SELECT vs_ok.stats
                     FROM video_stats vs_ok
                     WHERE vs_ok.url_id = u.id AND vs_ok.status = true
                     ORDER BY vs_ok.created_at DESC
                     LIMIT 1
                   ) AS last_successful_views
            FROM urls u
            JOIN platforms p ON p.id = u.platform_id
            LEFT JOIN LATERAL (
                SELECT vs.status, vs.stats
                FROM video_stats vs
                WHERE vs.url_id = u.id
                ORDER BY vs.created_at DESC
                LIMIT 1
            ) latest ON true
            WHERE u.telegram_user_id = ?
            ORDER BY u.created_at DESC
        """;

        List<UrlStatsRow> result = new ArrayList<>();
        try (Connection conn = Postgresql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, telegramUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Platform platform = mapDbPlatformNameToEnum(rs.getString("platform_name"));
                Long lastSuccessfulViews = (Long) rs.getObject("last_successful_views");
                boolean latestStatus = rs.getBoolean("latest_status");
                String latestStatusText = latestStatus ? "ok" : "source_unavailable";
                result.add(new UrlStatsRow(rs.getString("url"), platform, lastSuccessfulViews, latestStatus, latestStatusText));
            }
        } catch (SQLException e) {
            BackendLogger.log(queryErr + "Failed to load stats: " + e.getMessage());
            throw new RuntimeException("Database error in getUrlStatsForUser", e);
        }
        return result;
    }

    @Override
    public UserSummary getSummaryForUser(long telegramUserId) {
        String sql = """
            SELECT COUNT(*) AS total_urls,
                   COALESCE(SUM(s.last_success), 0) AS total_views
            FROM urls u
            LEFT JOIN LATERAL (
                SELECT vs_ok.stats AS last_success
                FROM video_stats vs_ok
                WHERE vs_ok.url_id = u.id AND vs_ok.status = true
                ORDER BY vs_ok.created_at DESC
                LIMIT 1
            ) s ON true
            WHERE u.telegram_user_id = ?
        """;
        try (Connection conn = Postgresql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, telegramUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserSummary(rs.getLong("total_urls"), rs.getLong("total_views"));
            }
        } catch (SQLException e) {
            BackendLogger.log(queryErr + "Failed to load summary: " + e.getMessage());
            throw new RuntimeException("Database error in getSummaryForUser", e);
        }
        return new UserSummary(0, 0);
    }

    @Override
    public void saveVideoStats(Map<String, Long> urlViewsMap, Map<String, Long> urlToIdMap) {
        String sql = "INSERT INTO video_stats (url_id, stats, status, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = Postgresql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int inserted = 0;
            for (var entry : urlViewsMap.entrySet()) {
                String url = entry.getKey();
                Long views = entry.getValue();
                Long urlId = urlToIdMap.get(url);
                if (urlId == null) {
                    BackendLogger.log(queryErr + "No id for URL: " + url);
                    continue;
                }
                boolean success = views != null && views >= 0;
                stmt.setLong(1, urlId);
                stmt.setLong(2, success ? views : 0L);
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
            case "vimeo" -> Platform.VIMEO;
            case "rutube" -> Platform.RUTUBE;
            default -> Platform.UNKNOWN;
        };
    }
}
