package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class RemoveClientsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallMapGenerator callMapGenerator;

    @Inject
    Provider<RemoveClientsTask> removeClientsTaskProvider;

    private CallDTO createdCallDTO;
    private Map<UUID, ClientDTO> createdClientDTOs;
    private Map<UUID, PeerConnectionDTO> createdPeerConnectionDTOs;

    @BeforeEach
    void setup() {
        this.callMapGenerator.generate();
        this.createdCallDTO = this.callMapGenerator.getCallDTO();
        this.createdClientDTOs = this.callMapGenerator.getClientDTOs();
        this.createdPeerConnectionDTOs = this.callMapGenerator.getPeerConnectionDTOs();
    }


    @Test
    public void removeClientDTOs_1() {
        var task = removeClientsTaskProvider.get()
                .whereClientIds(this.createdClientDTOs.keySet())
                .execute()
                ;

        this.createdClientDTOs.forEach((clientId, clientDTO) -> {
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
        var task = removeClientsTaskProvider.get();
        this.createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        this.createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasClient = this.hazelcastMaps.getClients().containsKey(clientId);
            Assertions.assertTrue(hasClient);
        });
    }

    @Test
    public void removeCallClientBindings_1() {
        var task = removeClientsTaskProvider.get()
                .whereClientIds(this.createdClientDTOs.keySet())
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(this.createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeCallClientBindings_2() {
        var task = removeClientsTaskProvider.get();
        this.createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(this.createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeClientsPeerConnectionBindings_1() {
        var task = removeClientsTaskProvider.get()
                .whereClientIds(this.createdClientDTOs.keySet())
                .execute()
                ;

        this.createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getClientToPeerConnectionIds().containsKey(clientId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removeClientsPeerConnectionBindings_2() {
        var task = removeClientsTaskProvider.get();
        this.createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        this.createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getClientToPeerConnectionIds().containsKey(clientId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionDTOs_1() {
        var task = removeClientsTaskProvider.get()
                .whereClientIds(this.createdClientDTOs.keySet())
                .execute()
                ;

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionDTOs_2() {
        var task = removeClientsTaskProvider.get();
        this.createdClientDTOs.values().forEach(task::addRemovedClientDTO);
        task.execute();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

}