package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.configs.CallFilterConfig;
import org.observertc.webrtc.observer.configs.CollectionFilterConfig;

import javax.inject.Inject;

@MicronautTest
class CallFilterConfigTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        CollectionFilterConfig browserIdsFilter = testUtils.generateCollectionFilter();
        CollectionFilterConfig peerConnectionsFilter = testUtils.generateCollectionFilter();
        CallFilterConfig callFilterConfig = CallFilterConfig.builder()
                .withName("name")
                .withCallName("callName")
                .withMarker("marker")
                .withServiceName("serviceName")
                .withBrowserIdsCollectionFilter(browserIdsFilter)
                .withPeerConnectionsCollectionFilter(peerConnectionsFilter)
                .build();

        Assertions.assertEquals("name", callFilterConfig.name);
        Assertions.assertEquals("callName", callFilterConfig.callName);
        Assertions.assertEquals("marker", callFilterConfig.marker);
        Assertions.assertEquals("serviceName", callFilterConfig.serviceName);
        Assertions.assertEquals(browserIdsFilter, callFilterConfig.browserIds);
        Assertions.assertEquals(peerConnectionsFilter, callFilterConfig.peerConnections);
    }

    @Test
    void shouldCheckEquality_1() {
        CallFilterConfig callFilterConfig_1 = testUtils.generateSentinelFilterDTO();
        CallFilterConfig callFilterConfig_2 = testUtils.generateSentinelFilterDTO();

        Assertions.assertEquals(callFilterConfig_1, callFilterConfig_1);
        Assertions.assertNotEquals(callFilterConfig_1, callFilterConfig_2);
    }

}