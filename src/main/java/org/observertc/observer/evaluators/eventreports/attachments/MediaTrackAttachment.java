package org.observertc.observer.evaluators.eventreports.attachments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.observertc.observer.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaTrackAttachment {

    private static final Logger logger = LoggerFactory.getLogger(MediaTrackAttachment.class);

    public static MediaTrackAttachment fromBase64(String input) {
        if (input == null) return null;
        try {
            return JsonUtils.base64ToObject(input, MediaTrackAttachment.class);
        } catch (Exception ex) {
            logger.warn("Exception while parsing");
            return null;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("streamDirection")
    public String streamDirection;

    @JsonProperty("mediaKind")
    public String mediaKind;

    @JsonProperty("sfuStreamId")
    public String sfuStreamId;

    @JsonProperty("sfuSinkId")
    public String sfuSinkId;

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

        private Builder from(MediaTrackAttachment source) {
            return this
                    .withSfuStreamId(source.sfuStreamId)
                    .withSfuSinkId(source.sfuSinkId)
                    .withStreamDirection(source.streamDirection)
                    .withMediaKind(source.mediaKind)
                    ;
        }

        public Builder withSfuStreamId(String value) {
            this.result.sfuStreamId = value;
            return this;
        }

        public Builder withSfuSinkId(String value) {
            this.result.sfuSinkId = value;
            return this;
        }

        public Builder withStreamDirection(String value) {
            this.result.streamDirection = value;
            return this;
        }

        public Builder withMediaKind(String value) {
            this.result.mediaKind = value;
            return this;
        }

        public MediaTrackAttachment build() {
            return this.result;
        }
    }
}
