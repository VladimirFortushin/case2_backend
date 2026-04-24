package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;

import java.util.List;
import java.util.Map;

public class VimeoApiClient extends BaseApiClient implements VideoPlatformClient {

    public VimeoApiClient(Http httpClient, String apiUrl, String token) {
        super(httpClient, apiUrl, token);
    }

    @Override
    public Platform getPlatform() {
        return Platform.VIMEO;
    }

    @Override
    public void updateViewsStats(List<String> urls, Map<String, Long> sink) {
        urls.forEach(url -> sink.put(url, -1L));
    }
}
