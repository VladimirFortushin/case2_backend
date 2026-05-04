package ru.mephi.case2.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import ru.mephi.case2.log.BackendLogger;

import java.util.Map;

public class JsonUtil {

    private static final Gson gson = new Gson();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String jsonParserErr = "[Json parser]: ERROR: ";

    public static String getFieldValue(String json, String key) {
        JsonElement element = JsonParser.parseString(json);
        return findValue(element, key);
    }

    public static <T> T jsonToPojo(String json, Class<T> cls) {
        try{
            return gson.fromJson(json, cls);
        }catch (JsonSyntaxException e){
            e.printStackTrace();
            BackendLogger.log(jsonParserErr + "Couldn't parse json into class " + cls.getName() + "\n" + json);
        }
        return null;
    }

    public static String flattenJson(String json) {
        try{
            return mapper.writeValueAsString(
                    mapper.readTree(json)
            );
        }catch (Exception e){

            BackendLogger.log(jsonParserErr + e.getMessage());
        }
        return null;
    }

    public static String pojoToJson(Object obj) {
        return gson.toJson(obj);
    }

    private static void updateValue(JsonElement element, String key, Object newValue) {
        if (element == null) return;

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            if (jsonObject.has(key)) {
                insertNewValue(jsonObject, key, newValue);
            }

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                updateValue(entry.getValue(), key, newValue);
            }

        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                updateValue(array.get(i), key, newValue);
            }
        }
    }

    private static void insertNewValue(JsonObject jsonObject, String key, Object newValue) {
        if (newValue == null) {
            jsonObject.addProperty(key, (String) null);
        } else if (newValue instanceof Number number) {
            jsonObject.addProperty(key, number);
        } else if (newValue instanceof Boolean b) {
            jsonObject.addProperty(key, b);
        } else if (newValue instanceof Character c) {
            jsonObject.addProperty(key, c);
        } else {
            jsonObject.addProperty(key, String.valueOf(newValue));
        }
    }

    private static String findValue(JsonElement element, String key) {
        String result = null;
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has(key)) {
                result = jsonObject.get(key).toString().replace("\"", "");
            }

            for (String innerKey : jsonObject.keySet()) {
                String innerResult = findValue(jsonObject.get(innerKey), key);
                if (innerResult != null) {
                    return innerResult;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                String arrayResult = findValue(arrayElement, key);
                if (arrayResult != null) {
                    return arrayResult;
                }
            }
        }
        return result;
    }
}
