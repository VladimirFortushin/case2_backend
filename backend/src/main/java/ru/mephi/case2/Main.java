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
import ru.mephi.case2.util.ApiConfig;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        //создание апи клиентов
        Http httpClient = HttpApiClient.getInstance();

        VideoPlatformClient youTubeApiClient = new YouTubeApiClient(httpClient, ApiConfig.getApiUrl(Platform.YOUTUBE),
                ApiConfig.getApiToken(Platform.YOUTUBE));

        VideoPlatformClient ruTubeApiClient = new RutubeApiClient(httpClient, ApiConfig.getApiUrl(Platform.RUTUBE),
                ApiConfig.getApiToken(Platform.RUTUBE));

        VideoPlatformClient vimeoApiClient = new VimeoApiClient(httpClient, ApiConfig.getApiUrl(Platform.VIMEO),
                ApiConfig.getApiToken(Platform.VIMEO));

        // регистрация и запуск расписания запроса урлов
        DbQueryScheduler scheduler = getDbQueryScheduler(youTubeApiClient, ruTubeApiClient, vimeoApiClient);

        scheduler.start();
    }

    private static DbQueryScheduler getDbQueryScheduler(VideoPlatformClient... videoPlatformClients) {
        List<VideoPlatformClient> clients = Arrays.asList(videoPlatformClients);
        // регистрация апи клиентов
        ApiClientRegistry registry = new ApiClientRegistry(clients);
        // репо, достающий из БД урлы видео
        DbVideoRepository repository = new DbVideoRepository();
        // инициализация слушателя
        ApiUpdateListener listener = new ApiUpdateListener(registry, repository);
        return new DbQueryScheduler(repository, listener);
    }
}
