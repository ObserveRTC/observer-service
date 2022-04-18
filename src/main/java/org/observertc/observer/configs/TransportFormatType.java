package org.observertc.observer.configs;

import java.util.Arrays;
import java.util.Locale;

public enum TransportFormatType {
    JSON,
    PROTOBUF,
    AVRO,
    NONE;

    public static TransportFormatType getValueOrDefault(String name, TransportFormatType defaultValue) {
        if (name == null) return defaultValue;
        return Arrays.stream(TransportFormatType.values())
                .filter(format -> format.name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(defaultValue);
    }
}
