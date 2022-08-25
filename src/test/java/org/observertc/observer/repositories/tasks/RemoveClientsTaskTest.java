package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.utils.DTOMapGenerator;

import java.util.Set;
import java.util.UUID;

@MicronautTest
class RemoveClientsTaskTest {

    @Inject
    HamokStorages hamokStorages;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();

    @Inject
    BeanProvider<RemoveClientsTask> removeClientsTaskProvider;


    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hamokStorages);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hamokStorages);
    }


    @Test
    public void removeClientDTOs_1() {
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get()
                .whereClientIds(createdClientDTOs.keySet())
                .execute()
                ;

        createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasClient = this.hamokStorages.getClients().containsKey(clientId);
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
            var hasClient = this.hamokStorages.getClients().containsKey(clientId);
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

        var hasCallId = this.hamokStorages.getCallToClientIds().containsKey(createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeCallClientBindings_2() {
        var createdCallDTO = dtoMapGenerator.getCallDTO();
        var createdClientDTOs = dtoMapGenerator.getClientDTOs();
        var task = removeClientsTaskProvider.get();
        createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        var hasCallId = this.hamokStorages.getCallToClientIds().containsKey(createdCallDTO.callId);
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
            var hasPeerConnections = this.hamokStorages.getClientToPeerConnectionIds().containsKey(clientId);
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
            var hasPeerConnections = this.hamokStorages.getClientToPeerConnectionIds().containsKey(clientId);
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
            var hasPeerConnections = this.hamokStorages.getPeerConnections().containsKey(peerConnectionId);
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
            var hasPeerConnections = this.hamokStorages.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void notCrashed_1() {
        var notExistingId = UUID.randomUUID();
        var task = removeClientsTaskProvider.get()
                .whereClientIds(Set.of(notExistingId))
                .execute()
                ;
    }

}