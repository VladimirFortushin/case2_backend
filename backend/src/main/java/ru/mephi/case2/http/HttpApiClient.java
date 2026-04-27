package ru.mephi.case2.http;

import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HttpApiClient implements Http {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final String httpOutGet = "[HTTP-GET]: OUT URL:";
    private final String httpInGet = "[HTTP-GET]: IN: ";
    private final String httpInPost = "[HTTP-POST]: IN: ";
    private final String httpOutPostUrl = "[HTTP-POST]: OUT URL: ";
    private final String httpOutPostBody = "[HTTP-POST]: OUT BODY: ";

    private static final Set<String> SENSITIVE_PARAM_NAMES = Set.of(
            "key", "token", "api_key", "access_token", "secret"
    );

    @Override
    public String doGet(String apiUrl, Map<String, String> headers, Map<String, String> queryParams) {
        try {
            String fullUrl = apiUrl + buildQueryString(queryParams);

            BackendLogger.log(httpOutGet + maskSensitiveParams(fullUrl));

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET();

            headers.forEach(builder::header);

            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            BackendLogger.log(httpInGet + JsonUtil.flattenJson(response.body()));
            return handleResponse(response, httpInGet);

        } catch (Exception e) {
            String maskedUrl = maskSensitiveParams(apiUrl + buildQueryString(queryParams));
            BackendLogger.log("GET request failed: " + maskedUrl);
            throw new RuntimeException("GET request failed", e);
        }
    }

    @Override
    public String doPost(String apiUrl, Map<String, String> headers, String body) {
        try {

            BackendLogger.log(httpOutPostUrl + apiUrl);
            BackendLogger.log(httpOutPostBody + body);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(body));

            headers.forEach(builder::header);

            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            BackendLogger.log(httpInPost + response.body());

            return handleResponse(response, httpInPost);

        } catch (Exception e) {
            throw new RuntimeException("POST request failed", e);
        }
    }

    private String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";

        return "?" + params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    private String handleResponse(HttpResponse<String> response, String logAppend) {
        int status = response.statusCode();
        if (status != 200) {
            BackendLogger.log(logAppend + "Status code: " + status);
            throw new RuntimeException(
                    "HTTP error: " + status + ", body: " + response.body()
            );
        }

        return response.body();
    }

    private String maskSensitiveParams(String url) {
        for (String param : SENSITIVE_PARAM_NAMES) {
            url = url.replaceAll("(?<=[?&]" + Pattern.quote(param) + "=)[^&]*", "***");
        }
        return url;
    }

    private static final HttpApiClient INSTANCE = new HttpApiClient();

    private HttpApiClient() {}

    public static HttpApiClient getInstance() {
        return INSTANCE;
    }

}
