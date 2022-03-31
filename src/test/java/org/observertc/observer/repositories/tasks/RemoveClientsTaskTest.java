package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class RemoveClientsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();

    @Inject
    Provider<RemoveClientsTask> removeClientsTaskProvider;


    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }


    @Test
    public void removeClientDTOs_1() {
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get()
                .whereClientIds(createdClientDTOs.keySet())
                .execute()
                ;

        createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasClient = this.hazelcastMaps.getClients().containsKey(clientId);
            Assertions.assertFalse(hasClient);
        });
    }

    /**
     * NOTE: if we add the peer connections and claim it is removed,
     * than the normal behavior is to assume it is removed
     */
    @Test
    public void removeClientDTOs_2() {
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get();
        createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasClient = this.hazelcastMaps.getClients().containsKey(clientId);
            Assertions.assertTrue(hasClient);
        });
    }

    @Test
    public void removeCallClientBindings_1() {
        var createdCallDTO = dtoMapGenerator.getCallDTO();
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get()
                .whereClientIds(createdClientDTOs.keySet())
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeCallClientBindings_2() {
        var createdCallDTO = dtoMapGenerator.getCallDTO();
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get();
        createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeClientsPeerConnectionBindings_1() {
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get()
                .whereClientIds(createdClientDTOs.keySet())
                .execute()
                ;

        createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getClientToPeerConnectionIds().containsKey(clientId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removeClientsPeerConnectionBindings_2() {
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get();
        createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getClientToPeerConnectionIds().containsKey(clientId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionDTOs_1() {
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var createdPeerConnectionDTOs = dtoMapGenerator.getPeerConnectionDTOs();
        var task = removeClientsTaskProvider.get()
                .whereClientIds(createdClientDTOs.keySet())
                .execute()
                ;

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionDTOs_2() {
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var createdPeerConnectionDTOs = dtoMapGenerator.getPeerConnectionDTOs();
        var task = removeClientsTaskProvider.get();
        createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

}