package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@MicronautTest
class PeerConnectionDTOTest {

    private static EasyRandom generator = new EasyRandom();

    @Test
    void shouldBuild_1() {
        UUID serviceUUID = UUID.randomUUID();
        UUID callUUID = UUID.randomUUID();
        UUID pcUUID = UUID.randomUUID();
        PeerConnectionDTO peerConnectionDTO = PeerConnectionDTO.of(serviceUUID,
                "serviceName",
                "mediaUnitId",
                callUUID,
                "callName",
                pcUUID,
                "userName",
                "browserId",
                "timeZone",
                1L,
                "marker");

        Assertions.assertEquals(serviceUUID, peerConnectionDTO.serviceUUID);
        Assertions.assertEquals("serviceName", peerConnectionDTO.serviceName);
        Assertions.assertEquals("mediaUnitId", peerConnectionDTO.mediaUnitId);
        Assertions.assertEquals(callUUID, peerConnectionDTO.callUUID);
        Assertions.assertEquals("callName", peerConnectionDTO.callName);
        Assertions.assertEquals(pcUUID, peerConnectionDTO.peerConnectionUUID);
        Assertions.assertEquals("userName", peerConnectionDTO.providedUserName);
        Assertions.assertEquals("timeZone", peerConnectionDTO.timeZone);
        Assertions.assertEquals(1L, peerConnectionDTO.joined);
        Assertions.assertEquals("marker", peerConnectionDTO.marker);

    }

    @Test
    void shouldCheckEquality_1() {
        PeerConnectionDTO peerConnectionDTO_1 = generator.nextObject(PeerConnectionDTO.class);
        PeerConnectionDTO peerConnectionDTO_2 = generator.nextObject(PeerConnectionDTO.class);

        Assertions.assertEquals(peerConnectionDTO_1, peerConnectionDTO_1);
        Assertions.assertNotEquals(peerConnectionDTO_1, peerConnectionDTO_2);
    }

}