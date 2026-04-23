package ru.mephi.case2.api;

import ru.mephi.case2.api.client.VideoPlatformClient;
import ru.mephi.case2.db.entity.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiUpdateListener {

    private final ApiClientRegistry registry;

    public ApiUpdateListener(ApiClientRegistry registry) {
        this.registry = registry;
    }

    public void triggerApiClientAction(Map<String, Platform> urlsWithPlatforms) {

        Map<Platform, List<String>> grouped = new HashMap<>();

        urlsWithPlatforms.forEach((url, platform) ->
                grouped.computeIfAbsent(platform, p -> new ArrayList<>()).add(url)
        );

        grouped.forEach((platform, urls) -> {
            VideoPlatformClient client = registry.get(platform);
            if (client != null) {
                client.updateStats(urls);
            }
        });
    }
}
