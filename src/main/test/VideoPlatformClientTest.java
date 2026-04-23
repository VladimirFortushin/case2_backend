import org.junit.jupiter.api.Test;
import ru.mephi.case2.api.client.YouTubeApiClient;
import ru.mephi.case2.http.Http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class VideoPlatformClientTest {
    @Test
    void shouldParseViewsFromResponse() {

        Http httpMock = mock(Http.class);

        String fakeJson = """
        {
          "items": [
            {
              "statistics": {
                "viewCount": "12345"
              }
            }
          ]
        }
        """;

        when(httpMock.doGet(anyString(), anyMap(), anyMap()))
                .thenReturn(fakeJson);

        YouTubeApiClient client =
                new YouTubeApiClient(httpMock, "https://fake-url");

        Integer views = client.getViewsStats(
                "token",
                "https://fake-url",
                "video123"
        );

        assertEquals(12345, views);

        verify(httpMock, times(1))
                .doGet(anyString(), anyMap(), anyMap());
    }
}
