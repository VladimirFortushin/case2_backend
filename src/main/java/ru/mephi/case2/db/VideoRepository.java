package ru.mephi.case2.db;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.db.entity.UrlInfo;
import ru.mephi.case2.db.entity.UrlStatsRow;
import ru.mephi.case2.db.entity.UserSummary;

import java.util.List;
import java.util.Map;

public interface VideoRepository {
    List<UrlInfo> getUrlsWithPlatforms();

    boolean addUrl(String url, Platform platform, long telegramUserId);

    List<UrlStatsRow> getUrlStatsForUser(long telegramUserId);

    UserSummary getSummaryForUser(long telegramUserId);

    void saveVideoStats(Map<String, Long> urlViewsMap, Map<String, Long> urlToIdMap);
}
