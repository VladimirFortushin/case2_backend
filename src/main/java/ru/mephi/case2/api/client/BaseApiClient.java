package ru.mephi.case2.api.client;

import ru.mephi.case2.http.Http;

public abstract class BaseApiClient {

    protected final Http httpClient;
    protected final String apiUrl;

    protected BaseApiClient(Http httpClient, String apiUrl) {
        this.httpClient = httpClient;
        this.apiUrl = apiUrl;
    }

    public abstract Integer getViewsStats(String token, String apiUrl, String videoLink);
    public abstract String parseVideoLinkFromUrl(String videoUrl);
    public abstract String getToken(String videoLink);
}
