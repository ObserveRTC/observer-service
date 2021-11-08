package org.observertc.webrtc.observer.evaluators.listeners.attachments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.observertc.webrtc.observer.common.JsonUtils;
import org.observertc.webrtc.observer.dto.StreamDirection;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RtpPadAttachment {

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("streamDirection")
    public String streamDirection;

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

        public RtpPadAttachment build() {
            return this.result;
        }
    }
}
