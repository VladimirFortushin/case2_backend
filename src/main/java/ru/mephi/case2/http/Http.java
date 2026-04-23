package ru.mephi.case2.http;

import java.util.Map;

public interface Http {
    String doGet(String url, Map<String, String> headers, Map<String, String> queryParams);

    String doPost(String url, Map<String, String> headers, String body);
}
