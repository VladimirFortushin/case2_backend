package ru.mephi.case2.scheduler;

import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.db.DbVideoRepository;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.db.entity.UrlInfo;
import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.BackendConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DbQueryScheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final DbVideoRepository videoRepository;
    private final ApiUpdateListener listener;

    public DbQueryScheduler(DbVideoRepository videoRepository,
                            ApiUpdateListener listener) {
        this.videoRepository = videoRepository;
        this.listener = listener;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                BackendLogger.log("[Scheduler] fetching urls...");
                List<UrlInfo> urlInfos = videoRepository.getUrlsWithPlatforms();
                if (!urlInfos.isEmpty()) {
                    listener.triggerUpdateAndSave(urlInfos);
                } else {
                    BackendLogger.log("[Scheduler] nothing to fetch");
                }
            } catch (Exception e) {
                BackendLogger.log("[Scheduler] ERROR: " + e.getMessage());
            }
        }, 0, BackendConfig.getStatsUpdateFrequency(), TimeUnit.SECONDS);
    }
}
