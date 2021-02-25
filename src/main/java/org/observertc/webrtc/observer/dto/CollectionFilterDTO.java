package org.observertc.webrtc.observer.dto;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;

import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class CollectionFilterDTO implements VersionedPortable {

    private static final int CLASS_VERSION = 1;

    public static Builder builder() { return new Builder();}

    @Min(-1)
    public int gt = -1;

    @Min(-1)
    public int eq = -1;

    @Min(-1)
    public int lt = -1;


    public String[] anyMatch = new String[0];
    public String[] allMatch = new String[0];



    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }
        CollectionFilterDTO otherDTO = (CollectionFilterDTO) other;
        if (this.gt != otherDTO.gt) return false;
        if (this.eq != otherDTO.eq) return false;
        if (this.lt != otherDTO.lt) return false;
        if (!Arrays.equals(this.allMatch, otherDTO.allMatch)) return false;
        if (!Arrays.equals(this.anyMatch, otherDTO.anyMatch)) return false;
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
        return PortableDTOFactory.COLLECTION_FILTER_DTO_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt("gt", this.gt);
        writer.writeInt("eq", this.eq);
        writer.writeInt("lt", this.lt);
        writer.writeUTFArray("anyMatch", this.anyMatch);
        writer.writeUTFArray("allMatch", this.allMatch);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.gt = reader.readInt("gt");
        this.eq = reader.readInt("eq");
        this.lt = reader.readInt("lt");
        this.anyMatch = reader.readUTFArray("anyMatch");
        this.allMatch = reader.readUTFArray("allMatch");
    }

    public static class Builder {
        private CollectionFilterDTO result = new CollectionFilterDTO();

        public Builder numOfElementsIsGreaterThan(int value) {
            this.result.gt = value;
            return this;
        }

        public Builder numOfElementsIsLessThan(int value) {
            this.result.lt = value;
            return this;
        }

        public Builder numOfElementsIsEqualTo(int value) {
            this.result.eq = value;
            return this;
        }

        public Builder anyOfTheElementsAreMatchingTo(String... elements) {
            Objects.requireNonNull(elements);
            this.result.anyMatch = elements;
            return this;
        }

        public Builder allOfTheElementsAreMatchingTo(String... elements) {
            Objects.requireNonNull(elements);
            this.result.allMatch = elements;
            return this;
        }

        public CollectionFilterDTO build() {
            return this.result;
        }
    }
}
