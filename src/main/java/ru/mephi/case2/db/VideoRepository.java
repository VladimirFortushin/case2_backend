package ru.mephi.case2.db;

import ru.mephi.case2.db.entity.Platform;

import java.util.Map;

public interface VideoRepository {
    Map<String, Platform> getUrlsWithPlatforms();
}
