package org.observertc.webrtc.observer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;

@JsonIgnoreProperties(value = { "classId", "factoryId", "classVersion" })
public class SentinelDTO implements VersionedPortable {
    private static final int CLASS_VERSION = 1;
    public static SentinelDTO.Builder builder() {
        return new SentinelDTO.Builder();
    }

    @NotNull
    public String name;
    public boolean report = false;
    public boolean expose = false;

    public boolean streamMetrics = false;

    public String[] anyMatchFilters = new String[0];
    public String[] allMatchFilters = new String[0];



    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }
        SentinelDTO otherDTO = (SentinelDTO) other;
        if (this.name != otherDTO.name) return false;
        if (this.report != otherDTO.report) return false;
        if (this.expose != otherDTO.expose) return false;
        if (this.streamMetrics != otherDTO.streamMetrics) return false;
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
        return PortableDTOFactory.SENTINEL_DTO_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("name", this.name);
        writer.writeBoolean("expose", this.expose);
        writer.writeBoolean("report", this.report);
        writer.writeBoolean("streamMetrics", this.streamMetrics);
        writer.writeUTFArray("anymatchFilters", this.anyMatchFilters);
        writer.writeUTFArray("allmatchFilters", this.allMatchFilters);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.name = reader.readUTF("name");
        this.expose = reader.readBoolean("expose");
        this.report = reader.readBoolean("report");
        this.streamMetrics = reader.readBoolean("streamMetrics");
        this.anyMatchFilters = reader.readUTFArray("anymatchFilters");
        this.allMatchFilters = reader.readUTFArray("allmatchFilters");
    }


    public static class Builder {
        private SentinelDTO result = new SentinelDTO();

        public Builder withReport(boolean value) {
            this.result.report = value;
            return this;
        }

        public Builder withExpose(boolean value) {
            this.result.expose = value;
            return this;
        }

        public Builder withStreamMetrics(boolean value) {
            this.result.streamMetrics = value;
            return this;
        }

        public Builder withAllMatchFilterNames(String... values) {
            this.result.allMatchFilters = values;
            return this;
        }

        public Builder withAnyMatchFilterNames(String... values) {
            this.result.anyMatchFilters = values;
            return this;
        }

        public SentinelDTO build() {
            return this.result;
        }
    }
}
