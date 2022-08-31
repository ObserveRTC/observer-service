//package org.observertc.observer.dto;
//
//import com.hazelcast.nio.serialization.PortableReader;
//import com.hazelcast.nio.serialization.PortableWriter;
//import org.observertc.observer.common.UUIDAdapter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.*;
//
//public class SerDeUtils {
//    private static final Logger logger = LoggerFactory.getLogger(SerDeUtils.class);
//    public static final String NULL_STRING = "00000000-0000-0000-0000-000000000000";
//    public static final String ESCAPED_NULL_STRING = "\\00000000-0000-0000-0000-000000000000";
//    public static final UUID NULL_UUID = UUID.fromString(NULL_STRING);
//
//
//    public static void writeLongArray(PortableWriter writer, String filedName, Collection<Long> source, long noItemsIndicator) {
//        long[] target;
//        if (0 < source.size()) {
//            target = new long[source.size()];
//            Iterator<Long> it = source.iterator();
//            for (int i = 0; it.hasNext(); ++i) {
//                target[i] = it.next();
//            }
//        } else {
//            target = new long[]{noItemsIndicator};
//        }
//        try {
//            writer.writeLongArray(filedName, target);
//        } catch (IOException e) {
//            logger.warn("Exception occurred while executing writeLongArray", e);
//        }
//    }
//
//    public static void readLongArray(PortableReader reader, String filedName, Collection<Long> target, long noItemsIndicator) {
//        long[] array = null;
//        try {
//            array = reader.readLongArray(filedName);
//        } catch (IOException e) {
//            logger.warn("Exception occurred while executing readLongArray", e);
//        }
//        if (Objects.isNull(array)) {
//            return;
//        }
//        if (array.length != 1 || array[0] != noItemsIndicator) {
//            Arrays.stream(array).forEach(target::add);
//        }
//    }
//
//    public static void writeNullableUUID(PortableWriter writer, String fieldName, UUID value) throws IOException {
//        byte[] bytes;
//        if (Objects.isNull(value)) {
//            bytes = UUIDAdapter.toBytes(NULL_UUID);
//        } else {
//            bytes = UUIDAdapter.toBytes(value);
//        }
//        writer.writeByteArray(fieldName, bytes);
//    }
//
//    public static UUID readNullableUUID(PortableReader reader, String fieldName) throws IOException {
//        UUID value = UUIDAdapter.toUUID(reader.readByteArray(fieldName));
//        if (value.equals(NULL_UUID)) {
//            return null;
//        }
//        return value;
//    }
//
//    public static void writeNullableString(PortableWriter writer, String fieldName, String value) throws IOException {
//        String toWrite;
//        if (Objects.isNull(value)) {
//            toWrite = NULL_STRING;
//        } else {
//            toWrite = value.replaceAll(NULL_STRING, ESCAPED_NULL_STRING);
//        }
//        writer.writeString(fieldName, toWrite);
//    }
//
//    public static String readNullableString(PortableReader reader, String fieldName) throws IOException {
//        String readValue = reader.readString(fieldName);
//        if (readValue.equals(NULL_STRING)) {
//            return null;
//        }
//        var result = readValue.replace(ESCAPED_NULL_STRING, NULL_STRING);
//        return result;
//    }
//
//    public static<T> T returnNullIf(T value, T nullIndicator) {
//        return value.equals(nullIndicator) ? null : value;
//    }
//
//}
