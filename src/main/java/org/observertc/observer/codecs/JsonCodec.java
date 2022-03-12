package org.observertc.observer.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonCodec<T> implements Codec<String, T> {
    private static final Logger logger = LoggerFactory.getLogger(JsonCodec.class);

    private final ObjectMapper mapper;
    private final Class<T> klass;

    public JsonCodec(Class<T> klass) {
        this(klass, new ObjectMapper());
    }

    public JsonCodec(Class<T> klass, ObjectMapper mapper) {
        this.klass = klass;
        this.mapper = mapper;
    }


    @Override
    public T encode(String data) {
        try {
            return this.mapper.readValue(data, this.klass);
        } catch (Exception ex) {
            logger.warn("Exception occurred while reading value {}", klass.getSimpleName(), ex);
            return null;
        }
    }

    @Override
    public String decode(T data) {
        try {
            return this.mapper.writeValueAsString(data);
        } catch (Exception ex) {
            logger.warn("Exception occurred writing value {}", klass.getSimpleName(), ex);
            return null;
        }
    }
}
