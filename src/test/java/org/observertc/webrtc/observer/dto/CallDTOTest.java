package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

@MicronautTest
class CallDTOTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        UUID callUUID = UUID.randomUUID();
        UUID serviceUUID = UUID.randomUUID();
        CallDTO callDTO = CallDTO.of(callUUID, serviceUUID, "serviceName", 1L, "callName", "marker");

        Assertions.assertEquals(callUUID, callDTO.callUUID);
        Assertions.assertEquals(serviceUUID, callDTO.serviceUUID);
        Assertions.assertEquals("serviceName", callDTO.serviceName);
        Assertions.assertEquals(1L, callDTO.initiated);
        Assertions.assertEquals("callName", callDTO.callName);
        Assertions.assertEquals("marker", callDTO.marker);

    }

}