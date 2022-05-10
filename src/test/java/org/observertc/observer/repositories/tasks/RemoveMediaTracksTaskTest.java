package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

import java.util.Set;
import java.util.UUID;

@MicronautTest
class RemoveMediaTracksTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();

    @Inject
    BeanProvider<RemoveMediaTracksTask> removeMediaTracksTaskProvider;

    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }


    @Test
    public void removeMediaTrackDTOs_1() {
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removeMediaTracksTaskProvider.get()
                .whereMediaTrackIds(createdMediaTrackDTOs.keySet())
                .execute()
                ;

        createdMediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
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
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removeMediaTracksTaskProvider.get();
        createdMediaTrackDTOs.values().forEach(task::addremovedMediaTrackDTO);
        task.execute();

        createdMediaTrackDTOs.forEach((trackId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getMediaTracks().containsKey(trackId);
            Assertions.assertTrue(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionInboundBindings_1() {
        var createdPeerConnectionDTOs = dtoMapGenerator.getPeerConnectionDTOs();
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removeMediaTracksTaskProvider.get()
                .whereMediaTrackIds(createdMediaTrackDTOs.keySet())
                .execute()
                ;

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionInboundBindings_2() {
        var createdPeerConnectionDTOs = dtoMapGenerator.getPeerConnectionDTOs();
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removeMediaTracksTaskProvider.get();
        createdMediaTrackDTOs.values().forEach(task::addremovedMediaTrackDTO);
        task.execute();

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionOutboundBindings_1() {
        var createdPeerConnectionDTOs = dtoMapGenerator.getPeerConnectionDTOs();
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removeMediaTracksTaskProvider.get()
                .whereMediaTrackIds(createdMediaTrackDTOs.keySet())
                .execute()
                ;

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void removePeerConnectionOutboundBindings_2() {
        var createdPeerConnectionDTOs = dtoMapGenerator.getPeerConnectionDTOs();
        var createdMediaTrackDTOs = dtoMapGenerator.getMediaTrackDTOs();
        var task = removeMediaTracksTaskProvider.get();
        createdMediaTrackDTOs.values().forEach(task::addremovedMediaTrackDTO);
        task.execute();

        createdPeerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var hasPeerConnections = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().containsKey(peerConnectionId);
            Assertions.assertFalse(hasPeerConnections);
        });
    }

    @Test
    public void notCrashed_1() {
        var notExistingId = UUID.randomUUID();
        var task = removeMediaTracksTaskProvider.get()
                .whereMediaTrackIds(Set.of(notExistingId))
                .execute()
                ;
    }

}