package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class HazelcastMapsTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void sanityTest() {

        Assertions.assertNotNull(hazelcastMaps.getCalls());
        Assertions.assertNotNull(hazelcastMaps.getCallToClientIds());
        Assertions.assertNotNull(hazelcastMaps.getServiceRoomToCallIds());
        Assertions.assertNotNull(hazelcastMaps.getClients());
        Assertions.assertNotNull(hazelcastMaps.getClientToPeerConnectionIds());
        Assertions.assertNotNull(hazelcastMaps.getPeerConnections());
        Assertions.assertNotNull(hazelcastMaps.getPeerConnectionToInboundTrackIds());
        Assertions.assertNotNull(hazelcastMaps.getPeerConnectionToOutboundTrackIds());
        Assertions.assertNotNull(hazelcastMaps.getMediaTracks());
        Assertions.assertNotNull(hazelcastMaps.getInboundTrackIdsToOutboundTrackIds());
        Assertions.assertNotNull(hazelcastMaps.getSFUs());
        Assertions.assertNotNull(hazelcastMaps.getSFUTransports());
        Assertions.assertNotNull(hazelcastMaps.getSFURtpPads());
        Assertions.assertNotNull(hazelcastMaps.getSfuStreamIdToRtpPadIds());
        Assertions.assertNotNull(hazelcastMaps.getSfuSinkIdToRtpPadIds());
        Assertions.assertNotNull(hazelcastMaps.getSfuStreams());
        Assertions.assertNotNull(hazelcastMaps.getSfuSinks());
        Assertions.assertNotNull(hazelcastMaps.getGeneralEntries());
        Assertions.assertNotNull(hazelcastMaps.getWeakLocks());
        Assertions.assertNotNull(hazelcastMaps.getSyncTaskStates());
        Assertions.assertNotNull(hazelcastMaps.getRequests());
        Assertions.assertNotNull(hazelcastMaps.getEtcMap());
    }

}