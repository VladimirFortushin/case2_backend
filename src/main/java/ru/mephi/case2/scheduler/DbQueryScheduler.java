package ru.mephi.case2.scheduler;

import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.db.DbVideoRepository;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.log.BackendLogger;

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
            BackendLogger.log("[Scheduler] fetching urls...");
            Map<String, Platform> urls = videoRepository.getUrlsWithPlatforms();
            if (!urls.isEmpty()) {
                listener.triggerApiClientAction(urls);
            } else {
                BackendLogger.log("[Scheduler] nothing to fetch");
            }
        }, 0, 2, TimeUnit.MINUTES);
    }
}
