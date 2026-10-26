package com.quizzlive.shared.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    public static String toJson(Object obj) {
        if (obj == null) return null;
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.trim().isEmpty()) return null;
        return gson.fromJson(json, classOfT);
    }

    public static Gson getGson() {
        return gson;
    }
}
