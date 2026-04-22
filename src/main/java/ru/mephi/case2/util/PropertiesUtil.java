package ru.mephi.case2.util;

import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PropertiesUtil {
    @Getter
    private static Map<String, String> properties = new HashMap<>();
    //TODO Актуализировать api url
    static {
        properties.put("youtubeApiUrl", "https://www.youtube.com/watch?v=");
        properties.put("rutubeApiUrl", "https://www.rutube.com/");
        properties.put("vimeoApiKey", "https://vimeo.com/");
    }
}
