package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
import ru.mephi.case2.http.HttpApiClient;
import ru.mephi.case2.util.ApiConfig;
import ru.mephi.case2.util.JsonUtil;

import java.util.List;
import java.util.Map;

public class YouTubeApiClient extends BaseApiClient implements VideoPlatformClient {


    public YouTubeApiClient(Http httpClient, String apiUrl) {
        super(httpClient, apiUrl);
    }

    @Override
    public Platform getPlatform() {
        return Platform.YOUTUBE;
    }

    @Override
    public void updateStats(List<String> urls) {

    }

    @Override
    public Integer getViewsStats(String token, String apiUrl, String videoLink) {

        String fullUrl = apiUrl +
                "?part=statistics&key=" + token +
                "&id=" + videoLink;

        String response = httpClient.doGet(fullUrl, Map.of(), Map.of());
        String views = JsonUtil.getFieldValue(response, "viewCount");
        return Integer.parseInt(views);
    }

    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        return "";
    }

    @Override
    public String getToken(String videoLink) {
        return "";
    }

}
