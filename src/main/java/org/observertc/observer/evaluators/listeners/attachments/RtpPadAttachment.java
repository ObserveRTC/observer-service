package org.observertc.observer.evaluators.listeners.attachments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.dto.StreamDirection;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RtpPadAttachment {

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("streamDirection")
    public String streamDirection;

    @JsonProperty("internal")
    public boolean internal;

    private RtpPadAttachment() {

    }

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }

    public String toBase64() {
        return JsonUtils.objectToBase64(this);
    }

    public static class Builder {
        private RtpPadAttachment result = new RtpPadAttachment();

        private Builder() {

        }

        public Builder withStreamDirection(StreamDirection streamDirection) {
            if (Objects.isNull(streamDirection)) {
                this.result.streamDirection = null;
            } else {
                this.result.streamDirection = streamDirection.name();
            }
            return this;
        }

        public Builder withInternal(boolean value) {
            this.result.internal = value;
            return this;
        }

        public RtpPadAttachment build() {
            return this.result;
        }
    }
}
