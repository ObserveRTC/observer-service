package org.observertc.observer.sources;

public class ReceivedMessage {
    public static ReceivedMessage of(String serviceId, String mediaUnitId, byte[] message) {
        var result = new ReceivedMessage();
        result.serviceId = serviceId;
        result.mediaUnitId = mediaUnitId;
        result.message = message;

        return result;
    }

    public String serviceId;
    public String mediaUnitId;
    public byte[] message;
}
