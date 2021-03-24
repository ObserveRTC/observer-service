package org.observertc.webrtc.observer.connectors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EncodedRecord {
    private static final String KEY_FIELD_NAME = "key";
    public static Builder builder() {
        return new Builder();
    }

    private byte[] message;
    private Map<String, Object> meta = new HashMap<>();

    public EncodedRecord(byte[] message) {
        this.message = message;
    }

    private EncodedRecord() {

    }

    public byte[] getMessage() {
        return this.message;
    }

    public UUID getKey() {
        return (UUID) this.meta.get(KEY_FIELD_NAME);
    }

    public static class Builder {
        private final EncodedRecord result = new EncodedRecord();

        public<T> Builder withMeta(String key, T value) {
            this.result.meta.put(key, value);
            return this;
        }

        public Builder withKey(UUID value) {
            this.result.meta.put(KEY_FIELD_NAME, value);
            return this;
        }

        public<T> Builder withMessage(byte[] message) {
            this.result.message = message;
            return this;
        }

        public EncodedRecord build() {
            return this.result;
        }
    }
}
