package org.observertc.webrtc.observer.evaluators.listeners.attachments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.observertc.webrtc.observer.common.JsonUtils;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaTrackAttachment {

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("streamDirection")
    public String streamDirection;

    @JsonProperty("rtpStreamId")
    public String rtpStreamId;

    private MediaTrackAttachment() {

    }

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }

    public String toBase64() {
        return JsonUtils.objectToBase64(this);
    }

    public static class Builder {
        private MediaTrackAttachment result = new MediaTrackAttachment();

        private Builder() {

        }

        public Builder fromBase64(String input) {
            if (Objects.isNull(input)) {
                return this;
            }
            MediaTrackAttachment source = JsonUtils.base64ToObject(input, MediaTrackAttachment.class);
            if (Objects.isNull(source)) {
                return this;
            }
            return this.from(source);
        }

        private Builder from(MediaTrackAttachment source) {
            return this.withRtpStreamId(source.rtpStreamId)
                    .withStreamDirection(source.streamDirection);
        }

        public Builder withRtpStreamId(String value) {
            this.result.rtpStreamId = value;
            return this;
        }

        public Builder withStreamDirection(String value) {
            this.result.streamDirection = value;
            return this;
        }

        public MediaTrackAttachment build() {
            return this.result;
        }
    }
}
