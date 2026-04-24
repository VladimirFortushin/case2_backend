package ru.mephi.case2.api.client;

import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
import ru.mephi.case2.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeApiClient extends BaseApiClient implements VideoPlatformClient {



    public YouTubeApiClient(Http httpClient, String apiUrl, String token) {
        super(httpClient, apiUrl, token);
    }


    @Override
    public Platform getPlatform() {
        return Platform.YOUTUBE;
    }
    //заполнение мапы у слушателя (урл - просмотры)
    @Override
    public void updateViewsStats(List<String> urls) {
        urls.forEach(url -> {
            Integer viewsCount = getViewsStats(url);
            ApiUpdateListener.getUrlViewsMap().put(url, viewsCount);
        });
    }
    //получение статы
    @Override
    public Integer getViewsStats(String videoUrl) {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("part", "statistics");
        requestParameters.put("key", token);
        requestParameters.put("id", parseVideoLinkFromUrl(videoUrl));
        String response = httpClient.doGet(apiUrl, Map.of(), requestParameters);
        String views = JsonUtil.getFieldValue(response, "viewCount");
        return Integer.parseInt(views);
    }

    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");

    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        Matcher m = YOUTUBE_PATTERN.matcher(videoUrl);
        return m.find() ? m.group(1) : "";
    }

    @Override
    public String getToken() {
        return "";
    }

}
