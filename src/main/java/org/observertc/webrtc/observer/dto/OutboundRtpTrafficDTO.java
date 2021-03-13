package org.observertc.webrtc.observer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;

import java.io.IOException;
import java.util.Objects;

@JsonIgnoreProperties(value = { "classId", "factoryId", "classVersion" })
public class OutboundRtpTrafficDTO implements VersionedPortable {
    private static final int CLASS_VERSION = 1;

    public static OutboundRtpTrafficDTO.Builder builder() {
        return new Builder();
    }

    public String peerConnectionUUID;
    public long SSRC;

    public long firstBytesSent = -1;
    public long lastBytesSent = -1;

    public int firstPacketsSent = -1;
    public int lastPacketsSent = -1;

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }

        OutboundRtpTrafficDTO otherDTO = (OutboundRtpTrafficDTO) other;
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
        return PortableDTOFactory.OUTBOUND_RTP_TRAFFIC_DTO_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("peerConnectionUUID", this.peerConnectionUUID);
        writer.writeLong("SSRC", this.SSRC);

        writer.writeLong("firstBytesSent", this.firstBytesSent);
        writer.writeLong("lastBytesSent", this.lastBytesSent);

        writer.writeInt("firstPacketsSent", this.firstPacketsSent);
        writer.writeInt("lastPacketsSent", this.lastPacketsSent);

    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {

        this.peerConnectionUUID = reader.readUTF("peerConnectionUUID");
        this.SSRC = reader.readLong("SSRC");

        this.firstBytesSent = reader.readLong("firstBytesSent");
        this.lastBytesSent = reader.readLong("lastBytesSent");

        this.firstPacketsSent = reader.readInt("firstPacketsSent");
        this.lastPacketsSent = reader.readInt("lastPacketsSent");

    }

    public static class Builder {
        private OutboundRtpTrafficDTO result = new OutboundRtpTrafficDTO();

        public Builder withPeerConnectionUUID(String pcUUID) {
            this.result.peerConnectionUUID = pcUUID;
            return this;
        }

        public Builder withSSRC(Long SSRC) {
            this.result.SSRC = SSRC;
            return this;
        }

        public OutboundRtpTrafficDTO build() {
            return this.result;
        }
    }
}
