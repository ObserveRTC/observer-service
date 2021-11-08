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
class RemoveMediaTracksTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallMapGenerator callMapGenerator;

    @Inject
    Provider<RemoveMediaTracksTask> removeMediaTracksTaskProvider;
    private Map<UUID, PeerConnectionDTO> createdPeerConnectionDTOs;
    private Map<UUID, MediaTrackDTO> createdMediaTrackDTOs;

    @BeforeEach
    void setup() {
        this.callMapGenerator.generate();
        this.createdPeerConnectionDTOs = this.callMapGenerator.getPeerConnectionDTOs();
        this.createdMediaTrackDTOs = this.callMapGenerator.getMediaTrackDTOs();
    }


    @Test
    public void removeMediaTrackDTOs_1() {
        var task = removeMediaTracksTaskProvider.get()
                .whereMediaTrackIds(this.createdMediaTrackDTOs.keySet())
                .execute()
                ;

        this.createdMediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getMediaTracks().containsKey(trackId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    /**
     * NOTE: in case of add removed connection, the correct assumption is
     * that the media track dto has already been removed
     */
    @Test
    public void removeMediaTrackDTOs_2() {
        var task = removeMediaTracksTaskProvider.get();
        this.createdMediaTrackDTOs.values().forEach(task::addremovedMediaTrackDTO);
        task.execute();

        this.createdMediaTrackDTOs.forEach((trackId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getMediaTracks().containsKey(trackId);
            Assertions.assertTrue(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionInboundBindings_1() {
        var task = removeMediaTracksTaskProvider.get()
                .whereMediaTrackIds(this.createdMediaTrackDTOs.keySet())
                .execute()
                ;

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionInboundBindings_2() {
        var task = removeMediaTracksTaskProvider.get();
        this.createdMediaTrackDTOs.values().forEach(task::addremovedMediaTrackDTO);
        task.execute();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionOutboundBindings_1() {
        var task = removeMediaTracksTaskProvider.get()
                .whereMediaTrackIds(this.createdMediaTrackDTOs.keySet())
                .execute()
                ;

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionOutboundBindings_2() {
        var task = removeMediaTracksTaskProvider.get();
        this.createdMediaTrackDTOs.values().forEach(task::addremovedMediaTrackDTO);
        task.execute();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

}