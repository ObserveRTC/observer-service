package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.PeerConnectionDTOGenerator;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

@MicronautTest
class AddPeerConnectionsTaskTest {

    @Inject
    Provider<AddPeerConnectionsTask> addPeerConnectionsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    PeerConnectionDTOGenerator generator;

    @Test
    public void inserted_1() {
        var peerConnectionDTO = generator.get();
        var task = addPeerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(Map.of(peerConnectionDTO.peerConnectionId, peerConnectionDTO));

        task.execute();

        var insertedPeerConnectionDTO = this.hazelcastMaps.getPeerConnections().get(peerConnectionDTO.peerConnectionId);
        Assertions.assertEquals(peerConnectionDTO, insertedPeerConnectionDTO);
    }

    @Test
    public void bound_1() {
        var peerConnectionDTO = generator.get();
        var task = addPeerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(Map.of(peerConnectionDTO.peerConnectionId, peerConnectionDTO));

        task.execute();

        var insertedPeerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(peerConnectionDTO.clientId);
        Assertions.assertTrue(insertedPeerConnectionIds.contains(peerConnectionDTO.peerConnectionId));
    }
}