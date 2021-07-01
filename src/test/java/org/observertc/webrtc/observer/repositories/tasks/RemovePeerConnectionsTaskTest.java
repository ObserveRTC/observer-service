package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class RemovePeerConnectionsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallMapGenerator callMapGenerator;

    @Inject
    Provider<RemovePeerConnectionsTask> removePeerConnectionsTaskProvider;

    private Map<UUID, PeerConnectionDTO> createdPeerConnectionDTOs;
    private Map<UUID, MediaTrackDTO> createdMediaTrackDTOs;

    @BeforeEach
    void setup() {
        this.callMapGenerator.generate();
        this.createdPeerConnectionDTOs = this.callMapGenerator.getPeerConnectionDTOs();
        this.createdMediaTrackDTOs = this.callMapGenerator.getMediaTrackDTOs();
    }


    @Test
    public void removePeerConnectionDTOs_1() {
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    /**
     * NOTE: in case of add removed connection, the correct assumption is
     * that the peer connection dto has already been removed
     */
    @Test
    public void removePeerConnectionDTOs_2() {
        var task = removePeerConnectionsTaskProvider.get();
        this.createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnections().containsKey(peerConnectionId);
            Assertions.assertTrue(hasPeerConnections);
        });
    }

    @Test
    public void removeClientPeerConnectionBindings_1() {
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasClientBinding = this.hazelcastMaps.getClientToPeerConnectionIds().containsKey(peerConnectionDTO.clientId);
            Assertions.assertFalse(hasClientBinding);
        });
    }

    @Test
    public void removeClientPeerConnectionBindings_2() {
        var task = removePeerConnectionsTaskProvider.get();
        this.createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasClientBinding = this.hazelcastMaps.getClientToPeerConnectionIds().containsKey(peerConnectionDTO.clientId);
            Assertions.assertFalse(hasClientBinding);
        });
    }

    @Test
    public void removeInboundMediaTrackBindings_1() {
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeInboundMediaTrackBindings_2() {
        var task = removePeerConnectionsTaskProvider.get();
        this.createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeOutboundMediaTrackBindings_1() {
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeOutboundMediaTrackBindings_2() {
        var task = removePeerConnectionsTaskProvider.get();
        this.createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasMediaTrackBinding = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasMediaTrackBinding);
        });
    }

    @Test
    public void removeMediaTrackDTOs_1() {
        var task = removePeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                .execute()
                ;

        this.createdMediaTrackDTOs.keySet().forEach(trackId -> {
            var hasMediaTrack = this.hazelcastMaps.getMediaTracks().containsKey(trackId);
            Assertions.assertFalse(hasMediaTrack);
        });
    }

    @Test
    public void removeMediaTrackDTOs_2() {
        var task = removePeerConnectionsTaskProvider.get();
        this.createdPeerConnectionDTOs.values().forEach(task::addRemovedPeerConnectionDTO);
        task.execute();

        this.createdMediaTrackDTOs.keySet().forEach(trackId -> {
            var hasMediaTrack = this.hazelcastMaps.getMediaTracks().containsKey(trackId);
            Assertions.assertFalse(hasMediaTrack);
        });
    }


}