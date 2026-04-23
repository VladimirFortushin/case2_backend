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

        String ytApiUrl = ApiConfig.getUrl(Platform.YOUTUBE);
        VideoPlatformClient youTubeApiClient = new YouTubeApiClient(httpClient, ytApiUrl);

        String rtApiUrl = ApiConfig.getUrl(Platform.RUTUBE);
        VideoPlatformClient ruTubeApiClient = new RutubeApiClient(httpClient, rtApiUrl);

        String vimApiUrl = ApiConfig.getUrl(Platform.VIMEO);
        VideoPlatformClient vimeoApiClient = new VimeoApiClient(httpClient, vimApiUrl);
        // регистрация и запуск расписания запроса урлов
        DbQueryScheduler scheduler = getDbQueryScheduler(youTubeApiClient, ruTubeApiClient, vimeoApiClient);

        scheduler.start();
    }

    private static DbQueryScheduler getDbQueryScheduler(VideoPlatformClient youTubeApiClient, VideoPlatformClient ruTubeApiClient, VideoPlatformClient vimeoApiClient) {
        List<VideoPlatformClient> clients = Arrays.asList(youTubeApiClient, ruTubeApiClient, vimeoApiClient);
        // регистрация апи клиентов
        ApiClientRegistry registry = new ApiClientRegistry(clients);
        // инициализация слушателя
        ApiUpdateListener listener = new ApiUpdateListener(registry);
        // репо, достающий из БД урлы видео
        DbVideoRepository repository = new DbVideoRepository();

        return new DbQueryScheduler(repository, listener);
    }
}
