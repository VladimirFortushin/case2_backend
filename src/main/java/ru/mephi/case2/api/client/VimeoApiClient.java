package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;

import java.util.List;

public class VimeoApiClient extends BaseApiClient implements VideoPlatformClient
{


    public VimeoApiClient(Http httpClient, String apiUrl, String token) {
        super(httpClient, apiUrl, token);
    }

    @Override
    public Platform getPlatform() {
        return Platform.VIMEO;
    }

    @Override
    public void updateViewsStats(List<String> urls) {

    }

    @Override
    public Integer getViewsStats(String videoUrl) {
        return 0;
    }

    @Override
    public String parseVideoLinkFromUrl(String videoUrl) {
        return "";
    }

    @Override
    public String getToken() {
        return "";
    }
}
