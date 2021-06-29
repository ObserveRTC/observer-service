package org.observertc.webrtc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServiceRoomIdTest {

    private String serviceId = "serviceId";
    private String roomId = "roomId";

    @Test
    public void shouldMakeServiceAndRoomId() {
        var serviceRoomId = ServiceRoomId.make(this.serviceId, this.roomId);

        Assertions.assertNotNull(serviceRoomId);
    }

    @Test
    public void getServiceId() {
        var serviceRoomId = ServiceRoomId.make(this.serviceId, this.roomId);

        Assertions.assertEquals(this.serviceId, serviceRoomId.serviceId);
    }

    @Test
    public void getRoomId() {
        var serviceRoomId = ServiceRoomId.make(this.serviceId, this.roomId);

        Assertions.assertEquals(this.roomId, serviceRoomId.roomId);
    }

    @Test
    public void equalFromKey_1() {
        var serviceRoomId_1 = ServiceRoomId.make(this.serviceId, this.roomId);
        var key = serviceRoomId_1.getKey();
        var serviceRoomId_2 = ServiceRoomId.fromKey(key);

        Assertions.assertEquals(serviceRoomId_1, serviceRoomId_2);
    }

    @Test
    public void equalFromKey_2() {
        var serviceRoomId_1 = ServiceRoomId.make(this.serviceId, this.roomId);
        var key = ServiceRoomId.createKey(serviceRoomId_1);
        var serviceRoomId_2 = ServiceRoomId.fromKey(key);

        Assertions.assertEquals(serviceRoomId_1, serviceRoomId_2);
    }
}