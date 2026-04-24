package ru.mephi.case2.api;

import lombok.Getter;
import ru.mephi.case2.api.client.VideoPlatformClient;
import ru.mephi.case2.db.DbVideoRepository;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.db.entity.UrlInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiUpdateListener {
    private final ApiClientRegistry registry;
    private final DbVideoRepository videoRepo;

    @Getter
    private static final Map<String, Integer> urlViewsMap = new ConcurrentHashMap<>();

    public ApiUpdateListener(ApiClientRegistry registry, DbVideoRepository videoRepo) {
        this.registry = registry;
        this.videoRepo = videoRepo;
    }

    public void triggerUpdateAndSave(List<UrlInfo> urlInfos) {
        // очистка мапы заданий по обновлению статы
        urlViewsMap.clear();

        // группировка платформ и урл + группировка урл и id
        Map<Platform, List<String>> platformUrlMap = new HashMap<>();
        Map<String, Long> urlToIdMap = new HashMap<>();

        for (var info : urlInfos) {
            urlToIdMap.put(info.url(), info.id());
            platformUrlMap.computeIfAbsent(info.platform(), p -> new ArrayList<>()).add(info.url());
        }

        // запуск клиентов – заполнение urlViewsMap
        platformUrlMap.forEach((platform, urls) -> {
            VideoPlatformClient client = registry.get(platform);
            if (client != null) {
                client.updateViewsStats(urls);
            }
        });

        // сохранение статы в БД
        videoRepo.saveVideoStats(urlViewsMap, urlToIdMap);

        // еще одна очистка мапы заданий по обновлению статы
        urlViewsMap.clear();
    }
}