package org.observertc.observer.components.eventreports.attachments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.observertc.observer.common.JsonUtils;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientAttachment {

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("timeZoneId")
    public String timeZoneId;

    private ClientAttachment() {

    }

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }

    public String toBase64() {
        return JsonUtils.objectToBase64(this);
    }

    public static class Builder {
        private ClientAttachment result = new ClientAttachment();

        private Builder() {

        }

        public Builder fromBase64(String input) {
            if (Objects.isNull(input)) {
                return this;
            }
            ClientAttachment source = JsonUtils.base64ToObject(input, ClientAttachment.class);
            if (Objects.isNull(source)) {
                return this;
            }
            return this.from(source);
        }

        private Builder from(ClientAttachment source) {
            return this.withTimeZoneId(source.timeZoneId);
        }

        public Builder withTimeZoneId(String value) {
            this.result.timeZoneId = value;
            return this;
        }

        public ClientAttachment build() {
            return this.result;
        }
    }
}
