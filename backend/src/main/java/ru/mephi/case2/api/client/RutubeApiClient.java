package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
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

    private static final Pattern RUTUBE_PATTERN = Pattern.compile("rutube\\.ru/video/([a-zA-Z0-9]+)/?");


    @Override
    public Platform getPlatform() {
        return Platform.RUTUBE;
    }

    @Override
    public Long getViewsStats(String videoUrl) {
        String videoId = parseVideoLinkFromUrl(videoUrl);
        String response = httpClient.doGet(apiUrl + "/" + videoId + "/", Map.of(), Map.of());
        String views = JsonUtil.getFieldValue(response, "hits");
        return Long.parseLong(views);
    }

    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        Matcher m = RUTUBE_PATTERN.matcher(videoUrl);
        return m.find() ? m.group(1) : "";
    }


    @Override
    public void updateApiToken() {}
}
