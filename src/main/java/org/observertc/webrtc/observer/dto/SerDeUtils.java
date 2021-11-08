package org.observertc.webrtc.observer.dto;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import org.observertc.webrtc.observer.common.UUIDAdapter;

import java.io.IOException;
import java.util.*;

public class SerDeUtils {
    public static final UUID NULL_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static void writeLongArray(PortableWriter writer, String filedName, Collection<Long> source, long noItemsIndicator) {
        long[] target;
        if (0 < source.size()) {
            target = new long[source.size()];
            Iterator<Long> it = source.iterator();
            for (int i = 0; it.hasNext(); ++i) {
                target[i] = it.next();
            }
        } else {
            target = new long[]{noItemsIndicator};
        }
        try {
            writer.writeLongArray(filedName, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readLongArray(PortableReader reader, String filedName, Collection<Long> target, long noItemsIndicator) {
        long[] array = null;
        try {
            array = reader.readLongArray(filedName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Objects.isNull(array)) {
            return;
        }
        if (array.length != 1 || array[0] != noItemsIndicator) {
            Arrays.stream(array).forEach(target::add);
        }
    }

    public static void writeNullableUUID(PortableWriter writer, String fieldName, UUID value) throws IOException {
        byte[] bytes;
        if (Objects.isNull(value)) {
            bytes = UUIDAdapter.toBytes(NULL_ID);
        } else {
            bytes = UUIDAdapter.toBytes(value);
        }
        writer.writeByteArray(fieldName, bytes);
    }

    public static UUID readNullableUUID(PortableReader reader, String fieldName) throws IOException {
        UUID value = UUIDAdapter.toUUID(reader.readByteArray(fieldName));
        if (value.equals(NULL_ID)) {
            return null;
        }
        return value;
    }

    public static<T> T returnNullIf(T value, T nullIndicator) {
        return value.equals(nullIndicator) ? null : value;
    }

}
