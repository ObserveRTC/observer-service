package org.observertc.observer.mappings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public final class SerDeUtils {

    private static final Logger logger = LoggerFactory.getLogger(SerDeUtils.class);

    public static Function<String, byte[]> createStrToByteFunc() {
        return str -> str.getBytes(StandardCharsets.UTF_8);
    }

    public static Function<byte[], String> createBytesToStr() {
        return bytes -> new String(bytes, StandardCharsets.UTF_8);
    }

    public static<T> Function<T, byte[]> createToJson() {
        return createToJson(new ObjectMapper(), logger);
    }

    public static<T> Function<T, byte[]> createToJson(ObjectMapper mapper) {
        return createToJson(mapper, logger);
    }

    public static<T> Function<T, byte[]> createToJson(ObjectMapper mapper, Logger logger) {
        return obj -> {
            try {
                return mapper.writeValueAsBytes(obj);
            } catch (JsonProcessingException e) {
                logger.error("Error occurred whilen serializing {}", obj, e);
                return null;
            }
        };
    }

    public static<T>  Function<byte[], T> createFromJson(Class<T> klass) {
        return createFromJson(klass, new ObjectMapper(), logger);
    }

    public static<T>  Function<byte[], T> createFromJson(Class<T> klass, ObjectMapper mapper) {
        return createFromJson(klass, mapper, logger);
    }

    public static<T, S extends T> Function<byte[], S> createFromJson(Class<T> klass, ObjectMapper mapper, Logger logger) {
        return obj -> {
            try {
                return (S) mapper.readValue(obj, klass);
            } catch (IOException e) {
                logger.error("Error occurred whilen serializing {}", obj, e);
                return null;
            }
        };
    }
}
