package dev.heowc.khpayment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String convertObjectToString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("json error");
        }
    }

    public static <T> T convertStringToObject(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("json error");
        }
    }

    private JsonUtil() {

    }
}
