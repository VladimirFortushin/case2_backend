package ru.mephi.case2;

import ru.mephi.case2.api.ApiClientRegistry;
import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.api.client.RutubeApiClient;
import ru.mephi.case2.api.client.VideoPlatformClient;
import ru.mephi.case2.api.client.VimeoApiClient;
import ru.mephi.case2.api.client.YouTubeApiClient;
import ru.mephi.case2.db.DbVideoRepository;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.Http;
import ru.mephi.case2.http.HttpApiClient;
import ru.mephi.case2.scheduler.DbQueryScheduler;
import ru.mephi.case2.service.LinkService;
import ru.mephi.case2.telegram.TelegramBotRunner;
import ru.mephi.case2.util.ApiConfig;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Http httpClient = HttpApiClient.getInstance();

        VideoPlatformClient youTubeApiClient = new YouTubeApiClient(httpClient,
                ApiConfig.getApiUrl(Platform.YOUTUBE), ApiConfig.getApiToken(Platform.YOUTUBE));

        VideoPlatformClient ruTubeApiClient = new RutubeApiClient(httpClient,
                ApiConfig.getApiUrl(Platform.RUTUBE), ApiConfig.getApiToken(Platform.RUTUBE));

        VideoPlatformClient vimeoApiClient = new VimeoApiClient(httpClient,
                ApiConfig.getApiUrl(Platform.VIMEO), ApiConfig.getApiToken(Platform.VIMEO));

        List<VideoPlatformClient> clients = Arrays.asList(youTubeApiClient, ruTubeApiClient, vimeoApiClient);
        ApiClientRegistry registry = new ApiClientRegistry(clients);
        DbVideoRepository repository = new DbVideoRepository();
        ApiUpdateListener listener = new ApiUpdateListener(registry, repository);

        DbQueryScheduler scheduler = new DbQueryScheduler(repository, listener);
        scheduler.start();

        String telegramToken = System.getenv("TELEGRAM_BOT_TOKEN");
        if (telegramToken != null && !telegramToken.isBlank()) {
            LinkService linkService = new LinkService(repository);
            TelegramBotRunner botRunner = new TelegramBotRunner(
                    telegramToken,
                    linkService,
                    listener,
                    TelegramBotRunner.parseAllowedUsers());
            botRunner.start();
        }
    }
}
