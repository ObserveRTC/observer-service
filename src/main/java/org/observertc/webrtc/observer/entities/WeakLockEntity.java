package org.observertc.webrtc.observer.entities;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import org.observertc.webrtc.observer.common.ObjectToString;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

public class WeakLockEntity implements Portable {
    public static final int CLASS_ID = 5000;
    private static final String NAME_FIELD_NAME = "name";
    private static final String INSTANCE_FIELD_NAME = "instance";
    private static final String CREATED_FIELD_NAME = "created";

    public static WeakLockEntity of(
            String name,
            String instance
    ) {
        WeakLockEntity result = new WeakLockEntity();
        result.instance = instance;
        result.name = name;
        return result;
    }

    public String name;
    public String instance;
    public Instant created = Instant.now();

    @Override
    public int getFactoryId() {
        return EntityFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
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
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof WeakLockEntity == false) {
            return false;
        }
        WeakLockEntity otherLock = (WeakLockEntity) other;
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
