package org.observertc.webrtc.observer.dto;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;

public class SentinelFilterDTO implements VersionedPortable {
    private static final int CLASS_VERSION = 1;

    public static SentinelFilterDTO.Builder builder() {
        return new Builder();
    }

    @NotNull
    public String name;

    @NotNull
    public String serviceName;

    public String callName = null;
    public String marker = null;

    public CollectionFilterDTO SSRCs = new CollectionFilterDTO();
    public CollectionFilterDTO browserIds =  new CollectionFilterDTO();
    public CollectionFilterDTO peerConnections = new CollectionFilterDTO();



    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }

        SentinelFilterDTO otherDTO = (SentinelFilterDTO) other;
        if (this.serviceName != otherDTO.serviceName) return false;
        if (!this.SSRCs.equals(otherDTO.SSRCs)) return false;
        if (!this.browserIds.equals(otherDTO.browserIds)) return false;
        if (!this.peerConnections.equals(otherDTO.peerConnections)) return false;
        return true;
    }


    @Override
    public int getClassVersion() {
        return CLASS_VERSION;
    }

    @Override
    public int getFactoryId() {
        return PortableDTOFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return PortableDTOFactory.SENTINEL_FILTER_DTO_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("name", this.name);
        writer.writeUTF("serviceName", this.serviceName);
        writer.writeUTF("callName", this.callName);
        writer.writeUTF("marker", this.marker);
        writer.writePortable("ssrcs", this.SSRCs);
        writer.writePortable("browserids", this.browserIds);
        writer.writePortable("peerconnections", this.peerConnections);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.name = reader.readUTF("name");
        this.serviceName = reader.readUTF("serviceName");
        this.callName = reader.readUTF("callName");
        this.marker = reader.readUTF("marker");
        this.SSRCs = reader.readPortable("ssrcs");
        this.browserIds = reader.readPortable("browserids");
        this.peerConnections = reader.readPortable("peerconnections");
    }

    public static class Builder {
        private SentinelFilterDTO result = new SentinelFilterDTO();

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

        public Builder withBrowserIdsCollectionFilter(CollectionFilterDTO value) {
            this.result.browserIds = value;
            return this;
        }

        public Builder withPeerConnectionsCollectionFilter(CollectionFilterDTO value) {
            this.result.peerConnections = value;
            return this;
        }

        public Builder withSSRCsCollectionFilter(CollectionFilterDTO value) {
            this.result.SSRCs = value;
            return this;
        }

        public SentinelFilterDTO build() {
            return this.result;
        }
    }
}
