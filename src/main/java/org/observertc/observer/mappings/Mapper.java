package org.observertc.observer.mappings;

import io.reactivex.rxjava3.functions.Function;
import org.bson.internal.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

public interface Mapper<U, R> {
    String BASE_64_MAPPER_LOGGER = "Base64Mapper";
    String STRING_TO_JSON_LOGGER = "StringToJsonMapper";
    String JSON_TO_STRING_LOGGER = "JsonToStringMapper";
    String STRING_TO_BYTES_MAPPER_LOGGER = "StringToBytesMapper";
    String BYTES_TO_STRING_MAPPER_LOGGER = "BytesToStringMapper";
    String DEFAULT_MAPPER_LOGGER = "DefaultMapperLogger";
    Map<String, Logger> loggers = Map.of(
            BASE_64_MAPPER_LOGGER, LoggerFactory.getLogger(BASE_64_MAPPER_LOGGER),
            STRING_TO_JSON_LOGGER, LoggerFactory.getLogger(STRING_TO_JSON_LOGGER),
            JSON_TO_STRING_LOGGER, LoggerFactory.getLogger(JSON_TO_STRING_LOGGER),
            STRING_TO_BYTES_MAPPER_LOGGER, LoggerFactory.getLogger(STRING_TO_BYTES_MAPPER_LOGGER),
            BYTES_TO_STRING_MAPPER_LOGGER, LoggerFactory.getLogger(BYTES_TO_STRING_MAPPER_LOGGER),
            DEFAULT_MAPPER_LOGGER, LoggerFactory.getLogger(Mapper.class)
    );

    static<T, U> Mapper<T, U> createFromRxFunc(io.reactivex.rxjava3.functions.Function<T, U> mapper, Logger _logger) {
        return new Mapper<T, U>() {
            @Override
            public U map(T data) {
                try {
                    return mapper.apply(data);
                } catch (Throwable ex) {
                    _logger.warn("Error occurred while encoding", ex);
                    return null;
                }
            }
        };
    }

    static<T, U> Mapper<T, U> createFromRxFunc(io.reactivex.rxjava3.functions.Function<T, U> encoder) {
        var logger = loggers.get(DEFAULT_MAPPER_LOGGER);
        return createFromRxFunc(encoder, logger);
    }

    static<T, U> Mapper<T, U> create(Function<T, U> mapper, Logger _logger) {
        return new Mapper<T, U>() {
            @Override
            public U map(T data) {
                try {
                    return mapper.apply(data);
                } catch (Throwable ex) {
                    _logger.warn("Error occurred while encoding", ex);
                    return null;
                }
            }
        };
    }

    static<T, U> Mapper<T, U> create(Function<T, U> encoder) {
        var logger = loggers.get(DEFAULT_MAPPER_LOGGER);
        return create(encoder, logger);
    }

    static Mapper<byte[], String> createBase64Mapper() {
        Logger logger = loggers.get(BASE_64_MAPPER_LOGGER);
        return create(Base64::encode, logger);
    }

    static Mapper<String, byte[]> createBytesToBase64Mapper() {
        Logger logger = loggers.get(BASE_64_MAPPER_LOGGER);
        return create(Base64::decode, logger);
    }

    static Mapper<String, byte[]> createStringToBytesMapper() {
        Logger logger = loggers.get(STRING_TO_BYTES_MAPPER_LOGGER);
        return create(String::getBytes, logger);
    }

    static Mapper<byte[], String> createBytesToStringMapper() {
        Logger logger = loggers.get(BYTES_TO_STRING_MAPPER_LOGGER);
        return create(String::new, logger);
    }

    static <U, R, V> Mapper<U, V> link(Mapper<U, R> actual, Mapper<R, V> next) {
        Objects.requireNonNull(actual);
        Objects.requireNonNull(next);
        Mapper<U, V> result = new Mapper<U, V>() {
            @Override
            public V map(U data) {
                var middle = actual.map(data);
                return next.map(middle);
            }
        };
        return result;
    }

    R map(U data);
}