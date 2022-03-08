package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

@MicronautTest
class AddMediaTracksTaskTest {

    @Inject
    Provider<AddMediaTracksTasks> addMediaTracksTasksProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var mediaTrackDTO = generator.getMediaTrackDTO();
        var task = addMediaTracksTasksProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var insertedMediaTrackDTO = this.hazelcastMaps.getMediaTracks().get(mediaTrackDTO.trackId);
        Assertions.assertEquals(mediaTrackDTO, insertedMediaTrackDTO);
    }

    @Test
    public void boundInboundMediaStreamToPeerConnection_1() {
        var mediaTrackDTO = generator.getMediaTrackDTOBuilder().withDirection(StreamDirection.INBOUND).build();
        var task = addMediaTracksTasksProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var peerConnectionMediaTracks = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(mediaTrackDTO.peerConnectionId);
        Assertions.assertTrue(peerConnectionMediaTracks.contains(mediaTrackDTO.trackId));
    }

    @Test
    public void boundOutboundMediaStreamToPeerConnection_1() {
        var mediaTrackDTO = generator.getMediaTrackDTOBuilder().withDirection(StreamDirection.OUTBOUND).build();
        var task = addMediaTracksTasksProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var peerConnectionMediaTracks = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(mediaTrackDTO.peerConnectionId);
        Assertions.assertTrue(peerConnectionMediaTracks.contains(mediaTrackDTO.trackId));
    }
}