package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RutubeApiClient extends BaseApiClient implements VideoPlatformClient
{
    public RutubeApiClient(Http httpClient, String apiUrl, String token) {
        super(httpClient, apiUrl, token);
    }
    private final String ruTubeLogAppender = "[API-CLIENT-RUTUBE]: ";
    private static final Pattern RUTUBE_PATTERN = Pattern.compile("rutube\\.ru/video/([a-zA-Z0-9]+)/?");


    @Override
    public Platform getPlatform() {
        return Platform.RUTUBE;
    }

    @Override
    public Long getViewsStats(String videoUrl) {
        String videoId = parseVideoLinkFromUrl(videoUrl);
        if (videoId == null || videoId.isBlank()) {
            BackendLogger.log(ruTubeLogAppender + "can't parse video link from URL: " + videoUrl);
            return -1L;
        }
        return  getVideoStats(videoId);
    }

    private Long getVideoStats(String videoId) {
        try {
            String response = httpClient.doGet(apiUrl + "/" + videoId + "/", Map.of(), Map.of());
            String views = JsonUtil.getFieldValue(response, "hits");
            if (views == null || views.isBlank() || views.equals("null")) return -1L;
            return Long.parseLong(views);
        } catch (Exception e) {
            BackendLogger.log(ruTubeLogAppender + "error: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        Matcher m = RUTUBE_PATTERN.matcher(videoUrl);
        return m.find() ? m.group(1) : "";
    }


    @Override
    public void updateApiToken() {}

    @Override
    public String getLogAppend() {
        return ruTubeLogAppender;
    }
}
