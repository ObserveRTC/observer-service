package org.observertc.observer.dto;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.observer.common.JsonUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

public class WeakLockDTO implements VersionedPortable {
    private static final int CLASS_VERSION = 1;
    private static final String NAME_FIELD_NAME = "name";
    private static final String INSTANCE_FIELD_NAME = "instance";
    private static final String CREATED_FIELD_NAME = "created";

    public static WeakLockDTO of(
            String name,
            String instance
    ) {
        WeakLockDTO result = new WeakLockDTO();
        result.instance = instance;
        result.name = name;
        return result;
    }

    public String name;
    public String instance;
    public Instant created = Instant.now();

    @Override
    public int getFactoryId() {
        return PortableDTOFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return PortableDTOFactory.WEAKLOCKS_DTO_CLASS_ID;
    }

    @Override
    public int getClassVersion() {
        return CLASS_VERSION;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(NAME_FIELD_NAME, this.name);
        writer.writeUTF(INSTANCE_FIELD_NAME, this.instance);
        long epochMilli = this.created.toEpochMilli();
        writer.writeLong(CREATED_FIELD_NAME, epochMilli);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.name = reader.readUTF(NAME_FIELD_NAME);
        this.instance = reader.readUTF(INSTANCE_FIELD_NAME);
        long epochMilli = reader.readLong(CREATED_FIELD_NAME);
        this.created = Instant.ofEpochMilli(epochMilli);
    }

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof WeakLockDTO == false) {
            return false;
        }
        WeakLockDTO otherLock = (WeakLockDTO) other;
        if (Objects.isNull(this.name)) {
            return  Objects.isNull(otherLock.name);
        } else if (!this.name.equals(otherLock.name)) {
            return false;
        }
        if (Objects.isNull(this.instance)) {
            return  Objects.isNull(otherLock.instance);
        } else if (!this.instance.equals(otherLock.instance)) {
            return false;
        }
        if (Objects.isNull(otherLock.created)) {
            return false;
        }
        long thisEpochMilli = this.created.toEpochMilli();
        long otherEpochMilli = otherLock.created.toEpochMilli();
        return thisEpochMilli == otherEpochMilli;
    }

}
