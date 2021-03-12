package org.observertc.webrtc.observer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;

import java.io.IOException;
import java.util.Objects;

@JsonIgnoreProperties(value = { "classId", "factoryId", "classVersion" })
public class InboundRtpTrafficDTO implements VersionedPortable {
    private static final int CLASS_VERSION = 1;

    public static InboundRtpTrafficDTO.Builder builder() {
        return new Builder();
    }

    public String peerConnectionUUID;
    public long SSRC;

    public long firstBytesReceived = -1;
    public long lastBytesReceived = -1;

    public int firstPacketsReceived = -1;
    public int lastPacketsReceived = -1;

    public int firstPacketsLost = -1;
    public int lastPacketsLost = -1;

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }

        InboundRtpTrafficDTO otherDTO = (InboundRtpTrafficDTO) other;
        if (this.peerConnectionUUID != otherDTO.peerConnectionUUID) return false;
        if (this.SSRC != otherDTO.SSRC) return false;
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
        return PortableDTOFactory.INBOUND_RTP_TRAFFIC_DTO_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("peerConnectionUUID", this.peerConnectionUUID);
        writer.writeLong("SSRC", this.SSRC);

        writer.writeLong("firstBytesReceived", this.firstBytesReceived);
        writer.writeLong("lastBytesReceived", this.lastBytesReceived);

        writer.writeInt("firstPacketsReceived", this.firstPacketsReceived);
        writer.writeInt("lastPacketsReceived", this.lastPacketsReceived);

        writer.writeInt("firstPacketsLost", this.firstPacketsLost);
        writer.writeInt("lastPacketsLost", this.lastPacketsLost);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {

        this.peerConnectionUUID = reader.readUTF("peerConnectionUUID");
        this.SSRC = reader.readLong("SSRC");

        this.firstBytesReceived = reader.readLong("firstBytesReceived");
        this.lastBytesReceived = reader.readLong("lastBytesReceived");

        this.firstPacketsReceived = reader.readInt("firstPacketsReceived");
        this.lastPacketsReceived = reader.readInt("lastPacketsReceived");

        this.firstPacketsLost = reader.readInt("firstPacketsLost");
        this.lastPacketsLost = reader.readInt("lastPacketsLost");

    }

    public static class Builder {
        private InboundRtpTrafficDTO result = new InboundRtpTrafficDTO();

        public Builder withPeerConnectionUUID(String pcUUID) {
            this.result.peerConnectionUUID = pcUUID;
            return this;
        }

        public Builder withSSRC(Long SSRC) {
            this.result.SSRC = SSRC;
            return this;
        }

        public InboundRtpTrafficDTO build() {
            return this.result;
        }
    }
}
