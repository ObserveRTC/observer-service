package org.observertc.webrtc.observer.connectors;

import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.schemas.reports.ReportType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EncodedRecord {
    private static final EncodedRecord EMPTY_RECORD = EncodedRecord.builder().withEmptyFlag().build();
    private static final String REPORT_KEY_FIELD_NAME = "reportKey";
    private static final String ENCODER_CLASS_FIELD_NAME = "encoderClass";
    private static final String MESSAGE_FORMAT_FIELD_NAME = "messageFormat";
    private static final String REPORT_TYPE_FIELD_NAME = "reportType";

    public static Builder builder() {
        return new Builder();
    }

    public static EncodedRecord ofEmpty() {
        return EMPTY_RECORD;
    }

    private boolean empty = false;

    private Object message;
    private Map<String, Object> meta = new HashMap<>();

    public EncodedRecord(byte[] message) {
        this.message = message;
    }

    private EncodedRecord() {

    }

    public boolean isEmpty() {
        return this.empty;
    }

    public MessageFormat getFormat() {
        return (MessageFormat) this.meta.get(MESSAGE_FORMAT_FIELD_NAME);
    }

    public Class getEncoderType() {
        return (Class) this.meta.get(ENCODER_CLASS_FIELD_NAME);
    }

    public<T> T getMessage() {
        return (T) this.message;
    }

    public UUID getKey() {
        return (UUID) this.meta.get(REPORT_KEY_FIELD_NAME);
    }

    public ReportType getReportType() { return (ReportType) this.meta.get(REPORT_TYPE_FIELD_NAME);}

    public static class Builder {
        private final EncodedRecord result = new EncodedRecord();

        public Builder withKey(UUID value) {
            this.result.meta.put(REPORT_KEY_FIELD_NAME, value);
            return this;
        }

        public Builder withMessage(Object message) {
            this.result.message = message;
            return this;
        }

        public Builder withEncoderType(Class klass) {
            this.result.meta.put(ENCODER_CLASS_FIELD_NAME, klass);
            return this;
        }

        public Builder withFormat(MessageFormat format) {
            this.result.meta.put(MESSAGE_FORMAT_FIELD_NAME, format);
            return this;
        }

        public Builder withReportType(ReportType type) {
            this.result.meta.put(REPORT_TYPE_FIELD_NAME, type);
            return this;
        }

        Builder withEmptyFlag() {
            this.result.empty = true;
            return this;
        }

        public EncodedRecord build() {
            if (this.result.empty) {
                return this.result;
            }
            if (Utils.anyNull(this.result.message,
                    this.result.meta.get(ENCODER_CLASS_FIELD_NAME),
                    this.result.meta.get(MESSAGE_FORMAT_FIELD_NAME)))
            {
                throw new IllegalStateException("Encoded message, encoder type, message format cannot be null");
            }
            return this.result;
        }


    }
}
