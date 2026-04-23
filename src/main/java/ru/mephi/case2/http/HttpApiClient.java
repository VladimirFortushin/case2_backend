package ru.mephi.case2.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpApiClient implements Http {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String doGet(String url, Map<String, String> headers, Map<String, String> queryParams) {
        try {
            String fullUrl = url + buildQueryString(queryParams);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET();

            headers.forEach(builder::header);

            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return handleResponse(response);

        } catch (Exception e) {
            throw new RuntimeException("GET request failed", e);
        }
    }

    @Override
    public String doPost(String url, Map<String, String> headers, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body));

            headers.forEach(builder::header);

            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return handleResponse(response);

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

    private String handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();

        if (status != 200) {
            throw new RuntimeException(
                    "HTTP error: " + status + ", body: " + response.body()
            );
        }

        return response.body();
    }

    private static final HttpApiClient INSTANCE = new HttpApiClient();

    private HttpApiClient() {}

    public static HttpApiClient getInstance() {
        return INSTANCE;
    }

}
