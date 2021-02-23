package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class SentinelFilterDTOTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        CollectionFilterDTO browserIdsFilter = testUtils.generateCollectionFilter();
        CollectionFilterDTO peerConnectionsFilter = testUtils.generateCollectionFilter();
        CollectionFilterDTO SSRCsFilter = testUtils.generateSSRCCollectionFilter();
        SentinelFilterDTO sentinelFilterDTO = SentinelFilterDTO.builder()
                .withName("name")
                .withCallName("callName")
                .withMarker("marker")
                .withServiceName("serviceName")
                .withBrowserIdsCollectionFilter(browserIdsFilter)
                .withPeerConnectionsCollectionFilter(peerConnectionsFilter)
                .withSSRCsCollectionFilter(SSRCsFilter)
                .build();

        Assertions.assertEquals("name", sentinelFilterDTO.name);
        Assertions.assertEquals("callName", sentinelFilterDTO.callName);
        Assertions.assertEquals("marker", sentinelFilterDTO.marker);
        Assertions.assertEquals("serviceName", sentinelFilterDTO.serviceName);
        Assertions.assertEquals(browserIdsFilter, sentinelFilterDTO.browserIds);
        Assertions.assertEquals(peerConnectionsFilter, sentinelFilterDTO.peerConnections);
        Assertions.assertEquals(SSRCsFilter, sentinelFilterDTO.SSRCs);
    }

    @Test
    void shouldCheckEquality_1() {
        SentinelFilterDTO sentinelFilterDTO_1 = testUtils.generateSentinelFilterDTO();
        SentinelFilterDTO sentinelFilterDTO_2 = testUtils.generateSentinelFilterDTO();

        Assertions.assertEquals(sentinelFilterDTO_1, sentinelFilterDTO_1);
        Assertions.assertNotEquals(sentinelFilterDTO_1, sentinelFilterDTO_2);
    }

}