package org.observertc.webrtc.observer.configs;

import org.observertc.webrtc.observer.common.ObjectToString;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class PeerConnectionFilterConfig {

    public static PeerConnectionFilterConfig.Builder builder() {
        return new Builder();
    }

    @NotNull
    public String name;

    @NotNull
    public String serviceName;

    public String callName = null;
    public String marker = null;

    public CollectionFilterConfig SSRCs = new CollectionFilterConfig();
    // version 2
    public CollectionFilterConfig remoteIPs = new CollectionFilterConfig();

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }

        PeerConnectionFilterConfig otherDTO = (PeerConnectionFilterConfig) other;
        if (this.serviceName != otherDTO.serviceName) return false;
        if (!this.SSRCs.equals(otherDTO.SSRCs)) return false;
        return true;
    }

    public static class Builder {
        private PeerConnectionFilterConfig result = new PeerConnectionFilterConfig();

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

        public Builder withRemoteIPs(CollectionFilterConfig value) {
            this.result.remoteIPs = value;
            return this;
        }

        public Builder withSSRCsCollectionFilter(CollectionFilterConfig value) {
            this.result.SSRCs = value;
            return this;
        }

        public PeerConnectionFilterConfig build() {
            return this.result;
        }
    }
}
