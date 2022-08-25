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
class RemovePeerConnectionsTaskTest {

    @Inject
    HamokStorages hamokStorages;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();

    @Inject
    BeanProvider<RemovePeerConnectionsTask> removePeerConnectionsTaskProvider;

    @BeforeEach
    void setup() {
        this.dtoMapGenerator.saveTo(hamokStorages);
    }

    @AfterEach
    void teardown() {
        this.dtoMapGenerator.deleteFrom(hamokStorages);
    }


    @Test
    public void removePeerConnectionDTOs_1() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hamokStorages.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    /**
     * NOTE: in case of add removed connection, the correct assumption is
     * that the peer connection dto has already been removed
     */
    @Test
    public void removePeerConnectionDTOs_2() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get();
        createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hamokStorages.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertTrue(hasPeerConnections);
        });
    }

    @Test
    public void removeClientPeerConnectionBindings_1() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasClientBinding = this.hamokStorages.getClientToPeerConnectionIds().containsKey(peerConnectionDTO.clientId);
            Assertions.assertFalse(hasClientBinding);
        });
    }

    @Test
    public void removeClientPeerConnectionBindings_2() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get();
        createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasClientBinding = this.hamokStorages.getClientToPeerConnectionIds().containsKey(peerConnectionDTO.clientId);
            Assertions.assertFalse(hasClientBinding);
        });
    }

    @Test
    public void removeInboundMediaTrackBindings_1() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hamokStorages.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeInboundMediaTrackBindings_2() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get();
        createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hamokStorages.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeOutboundMediaTrackBindings_1() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hamokStorages.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeOutboundMediaTrackBindings_2() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var task = removePeerConnectionsTaskProvider.get();
        createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hamokStorages.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeMediaTrackDTOs_1() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        createdMediaTrackDTOs.keySet().forEach(trackId -> {
            var hasMediaTrack = this.hamokStorages.getMediaTracks().containsKey(trackId);
            Assertions.assertFalse(hasMediaTrack);
        });
    }

    @Test
    public void removeMediaTrackDTOs_2() {
        var createdPeerConnectionDTOs = this.dtoMapGenerator.getPeerConnectionDTOs();
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removePeerConnectionsTaskProvider.get();
        createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        createdMediaTrackDTOs.keySet().forEach(trackId -> {
            var hasMediaTrack = this.hamokStorages.getMediaTracks().containsKey(trackId);
            Assertions.assertFalse(hasMediaTrack);
        });
    }


    @Test
    public void notCrashed_1() {
        var notExistingId = UUID.randomUUID();
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(Set.of(notExistingId))
                .execute()
                ;
    }
}