package ru.mephi.case2.api;

import ru.mephi.case2.api.client.VideoPlatformClient;
import ru.mephi.case2.db.entity.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClientRegistry {
    private final Map<Platform, VideoPlatformClient> map = new HashMap<>();

    public ApiClientRegistry(List<VideoPlatformClient> clients) {
        for (VideoPlatformClient client : clients) {
            map.put(client.getPlatform(), client);
        }
    }

    public VideoPlatformClient get(Platform platform) {
        return map.get(platform);
    }
}
