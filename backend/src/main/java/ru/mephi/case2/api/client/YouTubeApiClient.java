package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeApiClient extends BaseApiClient implements VideoPlatformClient {

    private final String youTubeLogAppender = "[API-CLIENT-YOUTUBE]: ";
    public YouTubeApiClient(Http httpClient, String apiUrl, String token) {
        super(httpClient, apiUrl, token);
    }

    private final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");


    @Override
    public Platform getPlatform() {
        return Platform.YOUTUBE;
    }
    //получение статы
    @Override
    public Long getViewsStats(String videoUrl) {
        String videoLink = parseVideoLinkFromUrl(videoUrl);
        if (videoLink == null || videoLink.isBlank()) {
            BackendLogger.log(youTubeLogAppender + "can't get video link from URL: " + videoUrl);
            return -1L;
        }
        Map<String, String> requestParameters = getRequestParameters(videoLink);
        return getApiResponse(requestParameters);
    }

    private Long getApiResponse(Map<String, String> requestParameters) {
        try {
            String response = httpClient.doGet(apiUrl, Map.of(), requestParameters);
            String views = JsonUtil.getFieldValue(response, "viewCount");
            if (views == null || views.isBlank() || views.equals("null")) return 0L;
            return Long.parseLong(views);
        } catch (Exception e) {
            BackendLogger.log(youTubeLogAppender + "error: " + e.getMessage());
            return 0L;
        }
    }

    private Map<String, String> getRequestParameters(String videoLink) {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("part", "statistics");
        requestParameters.put("key", token);
        requestParameters.put("id", videoLink);
        return requestParameters;
    }

    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        Matcher m = YOUTUBE_PATTERN.matcher(videoUrl);
        return m.find() ? m.group(1) : "";
    }

    @Override
    public void updateApiToken() {}

    @Override
    public String getLogAppend() {
        return youTubeLogAppender;
    }

}
