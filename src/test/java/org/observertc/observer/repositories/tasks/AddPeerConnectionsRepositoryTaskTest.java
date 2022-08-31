package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.DTOGenerators;
import org.observertc.observer.utils.TestUtils;

import java.util.Map;
import java.util.UUID;

@MicronautTest
class AddPeerConnectionsRepositoryTaskTest {

    @Inject
    BeanProvider<AddPeerConnectionsTask> addPeerConnectionsTaskProvider;

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var expected = generator.getPeerConnectionDTO();
        var task = addPeerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(Map.of(expected.peerConnectionId, expected));

        task.execute();

        var actual = this.hazelcastMaps.getPeerConnections().get(expected.peerConnectionId);
        var equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void boundToClient_1() {
        var peerConnectionDTO = generator.getPeerConnectionDTO();
        var task = addPeerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(Map.of(peerConnectionDTO.peerConnectionId, peerConnectionDTO));

        task.execute();

        var peerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(peerConnectionDTO.clientId);
        var contains = peerConnectionIds.contains(peerConnectionDTO.peerConnectionId);
        Assertions.assertTrue(contains);
    }

    @Test
    public void notCrashed_1() {
        var id = UUID.randomUUID();
        var task = addPeerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(TestUtils.nullValuedMap(id));

        task.execute();

        var actual = this.hazelcastMaps.getPeerConnections().get(id);
        Assertions.assertNull(actual);
    }
}