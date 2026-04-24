package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;

import java.util.List;
import java.util.Map;

public interface VideoPlatformClient {
    Platform getPlatform();

    void updateViewsStats(List<String> urls, Map<String, Long> sink);
}
