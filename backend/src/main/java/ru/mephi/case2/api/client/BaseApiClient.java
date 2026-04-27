package ru.mephi.case2.api.client;

import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.http.Http;

import java.util.List;

public abstract class BaseApiClient {

    protected final Http httpClient;
    protected final String apiUrl;
    protected final String token;

    protected BaseApiClient(Http httpClient, String apiUrl, String token) {
        this.httpClient = httpClient;
        this.apiUrl = apiUrl;
        this.token = token;
    }

    public abstract Long getViewsStats(String videoUrl);
    public abstract String parseVideoLinkFromUrl(String videoUrl);
    public abstract void updateApiToken();
    public void updateViewsStats(List<String> urls) {
        urls.forEach(url -> {
            Long viewsCount = getViewsStats(url);
            ApiUpdateListener.getUrlViewsMap().put(url, viewsCount);
        });
    }
}
