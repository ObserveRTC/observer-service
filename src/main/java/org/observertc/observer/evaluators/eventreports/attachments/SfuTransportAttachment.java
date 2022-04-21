package org.observertc.observer.evaluators.eventreports.attachments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.observertc.observer.common.JsonUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SfuTransportAttachment {

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("internal")
    public boolean internal;

    private SfuTransportAttachment() {

    }

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }

    public String toBase64() {
        return JsonUtils.objectToBase64(this);
    }

    public static class Builder {
        private SfuTransportAttachment result = new SfuTransportAttachment();

        private Builder() {

        }

        public Builder withInternal(boolean value) {
            this.result.internal = value;
            return this;
        }

        public SfuTransportAttachment build() {
            return this.result;
        }
    }
}
