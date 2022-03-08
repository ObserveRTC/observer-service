package org.observertc.observer.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperInstance {
    private static final ObjectMapper INSTANCE = new ObjectMapper();
    public static final ObjectMapper get() {
        return INSTANCE;
    }
}
