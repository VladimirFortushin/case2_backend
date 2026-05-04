import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.mephi.case2.api.client.YouTubeApiClient;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.HttpApiClient;
import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.ApiConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class YouTubeApiClientIntegrationTest {
    @Mock
    private HttpApiClient httpApiClient;

    private  YouTubeApiClient youTubeApiClient;
    private final String apiUrl =  ApiConfig.getApiUrl(Platform.YOUTUBE);
    private final String token = ApiConfig.getApiToken(Platform.YOUTUBE);
    private final String testErr = "[TEST]: ERROR ";
    private final String testSuccess = "[TEST]: SUCCESS ";
    @BeforeEach
    public void setup() {
        youTubeApiClient = new YouTubeApiClient(httpApiClient, apiUrl, token);
    }

    @Test
    void getRealStats() {
        if (token == null || token.isBlank()) {
            BackendLogger.log(testErr + "token is null or blank");
            return;
        }

        YouTubeApiClient client = new YouTubeApiClient(
                HttpApiClient.getInstance(), apiUrl, token);

        String videoUrl = "https://www.youtube.com/watch?v=jNQXAC9IVRw";
        Long views = client.getViewsStats(videoUrl);


        assertTrue(views != null && views > 0,
                testErr + "expected views > 0, but got " + views);

        BackendLogger.log(testSuccess + "video " +  videoUrl + " views: " + views);

    }

}
