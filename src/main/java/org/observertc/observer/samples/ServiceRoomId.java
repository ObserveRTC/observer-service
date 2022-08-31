package org.observertc.observer.samples;

import org.observertc.observer.common.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ServiceRoomId {
    private static final String DELIMITER = "##://:##";
    public static String createKey(ServiceRoomId serviceRoomId) {
        return String.join(DELIMITER, serviceRoomId.serviceId, serviceRoomId.roomId);
    }

    public static ServiceRoomId fromKey(String serviceRoomIdKey) {
        String[] parts = serviceRoomIdKey.split(DELIMITER);
        return new ServiceRoomId(parts[0], parts[1]);
    }

    public static ServiceRoomId fromBytes(byte[] bytes) {
        var key = new String(bytes, StandardCharsets.UTF_8);
        return fromKey(key);
    }

    public static ServiceRoomId make(String serviceId, String roomId) {
        return new ServiceRoomId(serviceId, roomId);
    }

    public final String serviceId;
    public final String roomId;

    public ServiceRoomId(String serviceId, String roomId) {
        this.serviceId = serviceId;
        this.roomId = roomId;
    }

    public byte[] toBytes() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.serviceId);
        buffer.append(DELIMITER);
        buffer.append(this.roomId);
        return buffer.toString().getBytes(StandardCharsets.UTF_8);
    }



    @Override
    public int hashCode() {
        return Objects.hash(this.serviceId, this.roomId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        ServiceRoomId otherServiceRoomId = (ServiceRoomId) other;
        if (!otherServiceRoomId.roomId.equals(this.roomId)) return false;
        if (!otherServiceRoomId.serviceId.equals(this.serviceId)) return false;
        return true;
    }

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }

    public String getKey() {
        return ServiceRoomId.createKey(this);
    }
}
