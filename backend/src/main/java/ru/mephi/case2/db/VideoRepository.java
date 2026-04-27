package ru.mephi.case2.db;

import ru.mephi.case2.db.entity.UrlInfo;

import java.util.List;

public interface VideoRepository {
    List<UrlInfo> getUrlsWithPlatforms();
}
