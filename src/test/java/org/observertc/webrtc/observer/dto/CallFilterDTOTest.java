package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class CallFilterDTOTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        CollectionFilterDTO browserIdsFilter = testUtils.generateCollectionFilter();
        CollectionFilterDTO peerConnectionsFilter = testUtils.generateCollectionFilter();
        CallFilterDTO callFilterDTO = CallFilterDTO.builder()
                .withName("name")
                .withCallName("callName")
                .withMarker("marker")
                .withServiceName("serviceName")
                .withBrowserIdsCollectionFilter(browserIdsFilter)
                .withPeerConnectionsCollectionFilter(peerConnectionsFilter)
                .build();

        Assertions.assertEquals("name", callFilterDTO.name);
        Assertions.assertEquals("callName", callFilterDTO.callName);
        Assertions.assertEquals("marker", callFilterDTO.marker);
        Assertions.assertEquals("serviceName", callFilterDTO.serviceName);
        Assertions.assertEquals(browserIdsFilter, callFilterDTO.browserIds);
        Assertions.assertEquals(peerConnectionsFilter, callFilterDTO.peerConnections);
    }

    @Test
    void shouldCheckEquality_1() {
        CallFilterDTO callFilterDTO_1 = testUtils.generateSentinelFilterDTO();
        CallFilterDTO callFilterDTO_2 = testUtils.generateSentinelFilterDTO();

        Assertions.assertEquals(callFilterDTO_1, callFilterDTO_1);
        Assertions.assertNotEquals(callFilterDTO_1, callFilterDTO_2);
    }

}