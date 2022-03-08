package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;

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
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var peerConnectionDTO = generator.getPeerConnectionDTO();
        var task = addPeerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(Map.of(peerConnectionDTO.peerConnectionId, peerConnectionDTO));

        task.execute();

        var insertedPeerConnectionDTO = this.hazelcastMaps.getPeerConnections().get(peerConnectionDTO.peerConnectionId);
        Assertions.assertEquals(peerConnectionDTO, insertedPeerConnectionDTO);
    }

    @Test
    public void bound_1() {
        var peerConnectionDTO = generator.getPeerConnectionDTO();
        var task = addPeerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(Map.of(peerConnectionDTO.peerConnectionId, peerConnectionDTO));

        task.execute();

        var insertedPeerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(peerConnectionDTO.clientId);
        Assertions.assertTrue(insertedPeerConnectionIds.contains(peerConnectionDTO.peerConnectionId));
    }
}