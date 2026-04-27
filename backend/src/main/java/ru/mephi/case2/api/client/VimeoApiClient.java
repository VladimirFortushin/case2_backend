package ru.mephi.case2.api.client;

import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
import ru.mephi.case2.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VimeoApiClient extends BaseApiClient implements VideoPlatformClient
{


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
        String videoId = parseVideoLinkFromUrl(videoUrl);

        String response = httpClient.doGet(
                apiUrl + "/" + videoId,
                Map.of("Authorization", "Bearer " + token),
                Map.of()
        );

        String views = JsonUtil.getFieldValue(response, "plays");

        if (views == null || views.isBlank()) {
            return -1L;
        }

        return Long.parseLong(views);
    }

    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        Matcher m = VIMEO_PATTERN.matcher(videoUrl);
        return m.find() ? m.group(1) : "";
    }

    @Override
    public void updateApiToken() {}
}
