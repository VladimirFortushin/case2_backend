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
import ru.mephi.case2.db.scheduler.DbQueryScheduler;
import ru.mephi.case2.util.BackendConfig;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // проверка наличия всех параметров
        BackendConfig.validateParams();

        // статический http инстанс
        Http httpClient = HttpApiClient.getInstance();

        // создание клиентов апи
        VideoPlatformClient youTubeApiClient = new YouTubeApiClient(httpClient, BackendConfig.getApiUrl(Platform.YOUTUBE),
                BackendConfig.getApiToken(Platform.YOUTUBE));

        VideoPlatformClient ruTubeApiClient = new RutubeApiClient(httpClient, BackendConfig.getApiUrl(Platform.RUTUBE),
                BackendConfig.getApiToken(Platform.RUTUBE));

        VideoPlatformClient vimeoApiClient = new VimeoApiClient(httpClient, BackendConfig.getApiUrl(Platform.VIMEO),
                BackendConfig.getApiToken(Platform.VIMEO));

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
