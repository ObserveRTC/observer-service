package org.observertc.observer.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonMapper {
    private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);

    public static<U> Mapper<U, String> createObjectToStringMapper(ObjectMapper mapper) {
        var result = Mapper.<U, String>create(mapper::writeValueAsString, logger);
        return result;
    }

    public static<U> Mapper<U, String> createObjectToStringMapper() {
        return createObjectToStringMapper(new ObjectMapper());
    }

    public static<U> Mapper<U, byte[]> createObjectToBytesMapper(ObjectMapper mapper) {
        var result = Mapper.<U, byte[]>create(mapper::writeValueAsBytes, logger);
        return result;
    }

    public static<U> Mapper<U, byte[]> createObjectToBytesMapper() {
        return createObjectToBytesMapper(new ObjectMapper());
    }

    public static<U> Mapper<String, U> createStringToObjectMapper(Class<U> klass, ObjectMapper mapper) {
        Function<String, U> func = (data) -> mapper.readValue(data, klass);
        var result = Mapper.<String, U>create(func, logger);
        return result;
    }

    public static<U> Mapper<String, U> createStringToObjectMapper(Class<U> klass) {
        return createStringToObjectMapper(klass, new ObjectMapper());
    }

    public static<U> Mapper<byte[], U> createBytesToObjectMapper(Class<U> klass, ObjectMapper mapper) {
        Function<byte[], U> func = (data) -> mapper.readValue(data, klass);
        var result = Mapper.<byte[], U>create(func, logger);
        return result;
    }

    public static<U> Mapper<byte[], U> createBytesToObjectMapper(Class<U> klass) {
        return createBytesToObjectMapper(klass, new ObjectMapper());
    }

    public static<U> Codec<U, byte[]> createBytesToObjectCodec(Class<U> klass) {
        return createBytesToObjectCodec(klass, new ObjectMapper());
    }

    public static<U> Codec<U, byte[]> createBytesToObjectCodec(Class<U> klass, ObjectMapper mapper) {
        Mapper<byte[], U> decoder = createBytesToObjectMapper(klass, mapper);
        Mapper<U, byte[]> encoder = createObjectToBytesMapper(mapper);
        var result = Codec.create(encoder, decoder);
        return result;
    }

    public static<U> Codec<U, String> createStringToObjectCodec(Class<U> klass) {
        return createStringToObjectCodec(klass, new ObjectMapper());
    }

    public static<U> Codec<U, String> createStringToObjectCodec(Class<U> klass, ObjectMapper mapper) {
        Mapper<String, U> decoder = createStringToObjectMapper(klass, mapper);
        Mapper<U, String> encoder = createObjectToStringMapper(mapper);
        var result = Codec.create(encoder, decoder);
        return result;
    }

    private JsonMapper() {

    }
}
