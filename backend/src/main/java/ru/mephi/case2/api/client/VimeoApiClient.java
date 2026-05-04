package ru.mephi.case2.api.client;

import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VimeoApiClient extends BaseApiClient implements VideoPlatformClient
{


    private final String vimeoLogAppender = "[API-CLIENT-VIMEO]: ";
    public VimeoApiClient(Http httpClient, String apiUrl, String token) {
        super(httpClient, apiUrl, token);
    }

    private static final Pattern VIMEO_PATTERN =
            Pattern.compile("vimeo\\.com/(\\d+)");

    @Override
    public Platform getPlatform() {
        return Platform.VIMEO;
    }

    @Override
    public Long getViewsStats(String videoUrl) {
        String videoLink = parseVideoLinkFromUrl(videoUrl);
        if (videoLink == null || videoLink.isBlank()) {
            BackendLogger.log(vimeoLogAppender + "can't parse video link from URL: " + videoUrl);
            return -1L;
        }
        return getVideoStats(videoLink);
    }

    private Long getVideoStats(String videoId) {
        try {
            String response = httpClient.doGet(
                    apiUrl + "/" + videoId,
                    Map.of("Authorization", "Bearer " + token),
                    Map.of());
            String views = JsonUtil.getFieldValue(response, "plays");
            if (views.equals("null") || views.isBlank()) {
                return 0L;
            }
            return Long.parseLong(views);
        } catch (Exception e) {
            BackendLogger.log(vimeoLogAppender + "error: " + e.getMessage());
            return 0L;
        }
    }


    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        Matcher m = VIMEO_PATTERN.matcher(videoUrl);
        return m.find() ? m.group(1) : "";
    }

    @Override
    public void updateApiToken() {}

    @Override
    public String getLogAppend() {
        return vimeoLogAppender;
    }
}
