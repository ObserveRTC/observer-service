package org.observertc.webrtc.observer.connectors;

import org.observertc.webrtc.observer.common.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EncodedRecord {
    private static final String KEY_FIELD_NAME = "key";
    public static Builder builder() {
        return new Builder();
    }

    private MessageFormat format;
    private byte[] message;
    private Map<String, Object> meta = new HashMap<>();

    public EncodedRecord(byte[] message) {
        this.message = message;
    }

    private EncodedRecord() {

    }

    public MessageFormat getFormat() {
        return this.format;
    }

    public byte[] getMessage() {
        return this.message;
    }

    public UUID getKey() {
        return (UUID) this.meta.get(KEY_FIELD_NAME);
    }

    public static class Builder {
        private final EncodedRecord result = new EncodedRecord();

        public Builder withKey(UUID value) {
            this.result.meta.put(KEY_FIELD_NAME, value);
            return this;
        }

        public Builder withMessage(byte[] message) {
            this.result.message = message;
            return this;
        }

        public Builder withFormat(MessageFormat format) {
            this.result.format = format;
            return this;
        }

        public EncodedRecord build() {
            if (Utils.anyNull(this.result.message)) {
                throw new IllegalStateException("Encoded message cannot be null");
            }
            return this.result;
        }
    }
}
