package ru.mephi.case2.api.client;

import ru.mephi.case2.db.entity.Platform;

import java.util.List;

public interface VideoPlatformClient {
    Platform getPlatform();
    void updateStats(List<String> urls);
}
