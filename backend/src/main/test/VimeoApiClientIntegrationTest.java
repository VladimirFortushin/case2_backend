import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.case2.api.client.VimeoApiClient;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.http.HttpApiClient;
import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.BackendConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class VimeoApiClientIntegrationTest {
    private VimeoApiClient client;
    private final String apiUrl = BackendConfig.getApiUrl(Platform.VIMEO);
    private final String token = BackendConfig.getApiToken(Platform.VIMEO);

    @BeforeEach
    void setup() {
        client = new VimeoApiClient(HttpApiClient.getInstance(), apiUrl, token);
    }

    @Test
    void getRealStats() {
        assumeTrue(token != null && !token.isBlank(), "Token is not set");

        String videoUrl = "https://vimeo.com/76979871";

        Long views = client.getViewsStats(videoUrl);

        assertTrue(views != null && views > 0,
                "Expected views > 0, but got " + views);

        BackendLogger.log("[TEST SUCCESS] views: " + views);
    }
}
