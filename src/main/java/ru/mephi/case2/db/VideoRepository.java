package ru.mephi.case2.db;

import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.db.entity.UrlInfo;

import java.util.List;
import java.util.Map;

public interface VideoRepository {
    List<UrlInfo> getUrlsWithPlatforms();
}
