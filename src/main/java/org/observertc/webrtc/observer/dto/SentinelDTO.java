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
    private static final int CLASS_VERSION = 3;


    public static SentinelDTO.Builder builder() {
        return new SentinelDTO.Builder();
    }

    @NotNull
    public String name;
    public boolean report = false;
    public boolean expose = false;

    // version 1 (only in migration it exists)
//    public String[] allmatchFilters = new String[0];
//    public String[] anymatchFilters = new String[0];

    public Filters callFilters = new Filters();
    public Filters pcFilters = new Filters();

    public static class Filters {
        public String[] anyMatch = new String[0];
        public String[] allMatch = new String[0];
    }

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

        if (this.getClassVersion() < 2) { // migration
            writer.writeUTFArray("allmatchFilters", this.callFilters.allMatch);
            writer.writeUTFArray("anymatchFilters", this.callFilters.anyMatch);

            return;
        }

        writer.writeUTFArray("calls_allmatch", this.callFilters.allMatch);
        writer.writeUTFArray("calls_anymatch", this.callFilters.anyMatch);

        writer.writeUTFArray("pcs_allmatch", this.pcFilters.allMatch);
        writer.writeUTFArray("pcs_anymatch", this.pcFilters.anyMatch);

    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.name = reader.readUTF("name");
        this.expose = reader.readBoolean("expose");
        this.report = reader.readBoolean("report");

        if (reader.getVersion() < 2) { // migration!
            this.callFilters.allMatch = reader.readUTFArray("allmatchFilters");
            this.callFilters.anyMatch = reader.readUTFArray("anymatchFilters");
            return;
        }

        if (Objects.isNull(this.callFilters)) {
            this.callFilters = new Filters();
        }
        this.callFilters.allMatch = reader.readUTFArray("calls_allmatch");
        this.callFilters.anyMatch = reader.readUTFArray("calls_anymatch");

        if (Objects.isNull(this.pcFilters)) {
            this.pcFilters = new Filters();
        }
        this.pcFilters.allMatch = reader.readUTFArray("pcs_allmatch");
        this.pcFilters.anyMatch = reader.readUTFArray("pcs_anymatch");

    }


    public static class Builder {
        private SentinelDTO result = new SentinelDTO();

        public Builder withName(String name) {
            this.result.name = name;
            return this;
        }

        public Builder withReport(boolean value) {
            this.result.report = value;
            return this;
        }

        public Builder withExpose(boolean value) {
            this.result.expose = value;
            return this;
        }

        public Builder withAllCallMatchFilterNames(String... values) {
            this.result.callFilters.allMatch = values;
            return this;
        }

        public Builder withAnyCallMatchFilterNames(String... values) {
            this.result.callFilters.anyMatch = values;
            return this;
        }

        public Builder withAllPCMatchFilterNames(String... values) {
            this.result.pcFilters.allMatch = values;
            return this;
        }

        public Builder withAnyPCMatchFilterNames(String... values) {
            this.result.pcFilters.anyMatch = values;
            return this;
        }

        public SentinelDTO build() {
            return this.result;
        }

    }

}
