package org.observertc.webrtc.observer.configs;

import org.observertc.webrtc.observer.common.ObjectToString;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CallFilterConfig {

    public static CallFilterConfig.Builder builder() {
        return new Builder();
    }

    @NotNull
    public String name;

    @NotNull
    public String serviceName;

    public String callName = null;
    public String marker = null;

    public CollectionFilterConfig browserIds =  new CollectionFilterConfig();
    public CollectionFilterConfig peerConnections = new CollectionFilterConfig();

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }

        CallFilterConfig otherDTO = (CallFilterConfig) other;
        if (this.serviceName != otherDTO.serviceName) return false;
        if (!this.browserIds.equals(otherDTO.browserIds)) return false;
        if (!this.peerConnections.equals(otherDTO.peerConnections)) return false;
        return true;
    }

    public static class Builder {
        private CallFilterConfig result = new CallFilterConfig();

        public Builder withServiceName(String value) {
            this.result.serviceName = value;
            return this;
        }

        public Builder withMarker(String value) {
            this.result.marker = value;
            return this;
        }

        public Builder withCallName(String value) {
            this.result.callName = value;
            return this;
        }

        public Builder withName(String value) {
            this.result.name = value;
            return this;
        }

        public Builder withBrowserIdsCollectionFilter(CollectionFilterConfig value) {
            this.result.browserIds = value;
            return this;
        }

        public Builder withPeerConnectionsCollectionFilter(CollectionFilterConfig value) {
            this.result.peerConnections = value;
            return this;
        }

        public CallFilterConfig build() {
            return this.result;
        }
    }
}
