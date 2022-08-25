package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class HamokStoragesTest {

    @Inject
    HamokStorages hamokStorages;

    @Test
    void sanityTest() {

        Assertions.assertNotNull(hamokStorages.getCalls());
        Assertions.assertNotNull(hamokStorages.getCallToClientIds());
        Assertions.assertNotNull(hamokStorages.getServiceRoomToCallIds());
        Assertions.assertNotNull(hamokStorages.getClients());
        Assertions.assertNotNull(hamokStorages.getClientToPeerConnectionIds());
        Assertions.assertNotNull(hamokStorages.getPeerConnections());
        Assertions.assertNotNull(hamokStorages.getPeerConnectionToInboundTrackIds());
        Assertions.assertNotNull(hamokStorages.getPeerConnectionToOutboundTrackIds());
        Assertions.assertNotNull(hamokStorages.getMediaTracks());
        Assertions.assertNotNull(hamokStorages.getInboundTrackIdsToOutboundTrackIds());
        Assertions.assertNotNull(hamokStorages.getSFUs());
        Assertions.assertNotNull(hamokStorages.getSFUTransports());
        Assertions.assertNotNull(hamokStorages.getSFURtpPads());
        Assertions.assertNotNull(hamokStorages.getSfuStreamIdToRtpPadIds());
        Assertions.assertNotNull(hamokStorages.getSfuSinkIdToRtpPadIds());
        Assertions.assertNotNull(hamokStorages.getSfuStreamIdToInternalOutboundRtpPadIds());
        Assertions.assertNotNull(hamokStorages.getSfuInternalInboundRtpPadIdToOutboundRtpPadId());
        Assertions.assertNotNull(hamokStorages.getSfuStreams());
        Assertions.assertNotNull(hamokStorages.getSfuSinks());
        Assertions.assertNotNull(hamokStorages.getGeneralEntries());
        Assertions.assertNotNull(hamokStorages.getWeakLocks());
        Assertions.assertNotNull(hamokStorages.getSyncTaskStates());
        Assertions.assertNotNull(hamokStorages.getRequests());
        Assertions.assertNotNull(hamokStorages.getEtcMap());

    }

}