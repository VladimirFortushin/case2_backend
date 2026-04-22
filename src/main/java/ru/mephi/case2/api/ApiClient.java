package ru.mephi.case2.api;

import ru.mephi.case2.db.entity.Platform;

import java.util.List;
import java.util.Map;

public interface ApiClient {
    Map<String, Platform> getUrlsWithPlatforms();
    Integer getViewsStats(String token, String apiUrl, String videoLink);
    String parseVideoLinkFromUrl(String videoUrl);
    void updateViewsStats(String videoLink, Integer viewsStats);
    String getToken(String videoLink);
}
