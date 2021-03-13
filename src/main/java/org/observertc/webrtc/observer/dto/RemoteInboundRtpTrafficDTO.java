package org.observertc.webrtc.observer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;

import java.io.IOException;
import java.util.Objects;

@JsonIgnoreProperties(value = { "classId", "factoryId", "classVersion" })
public class RemoteInboundRtpTrafficDTO implements VersionedPortable {
    private static final int CLASS_VERSION = 1;

    public static RemoteInboundRtpTrafficDTO.Builder builder() {
        return new Builder();
    }

    public String peerConnectionUUID;
    public long SSRC;

    public double rttAvg = -1.0;

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }

        RemoteInboundRtpTrafficDTO otherDTO = (RemoteInboundRtpTrafficDTO) other;
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
        return PortableDTOFactory.REMOTE_INBOUND_RTP_TRAFFIC_DTO_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("peerConnectionUUID", this.peerConnectionUUID);
        writer.writeLong("SSRC", this.SSRC);

        writer.writeDouble("rttAvg", this.rttAvg);

    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {

        this.peerConnectionUUID = reader.readUTF("peerConnectionUUID");
        this.SSRC = reader.readLong("SSRC");

        this.rttAvg = reader.readDouble("rttAvg");

    }

    public static class Builder {
        private RemoteInboundRtpTrafficDTO result = new RemoteInboundRtpTrafficDTO();

        public Builder withPeerConnectionUUID(String pcUUID) {
            this.result.peerConnectionUUID = pcUUID;
            return this;
        }

        public Builder withSSRC(Long SSRC) {
            this.result.SSRC = SSRC;
            return this;
        }

        public RemoteInboundRtpTrafficDTO build() {
            return this.result;
        }
    }
}
