package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.MediaTrackDTOGenerator;
import org.observertc.webrtc.observer.dto.StreamDirection;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

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
    MediaTrackDTOGenerator generator;

    @Test
    public void inserted_1() {
        var mediaTrackDTO = generator.get();
        var task = addMediaTracksTasksProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var insertedMediaTrackDTO = this.hazelcastMaps.getMediaTracks().get(mediaTrackDTO.trackId);
        Assertions.assertEquals(mediaTrackDTO, insertedMediaTrackDTO);
    }

    @Test
    public void boundInboundMediaStreamToPeerConnection_1() {
        var mediaTrackDTO = generator.withStreamDirection(StreamDirection.INBOUND).get();
        var task = addMediaTracksTasksProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var peerConnectionMediaTracks = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(mediaTrackDTO.peerConnectionId);
        Assertions.assertTrue(peerConnectionMediaTracks.contains(mediaTrackDTO.trackId));
    }

    @Test
    public void boundOutboundMediaStreamToPeerConnection_1() {
        var mediaTrackDTO = generator.withStreamDirection(StreamDirection.OUTBOUND).get();
        var task = addMediaTracksTasksProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var peerConnectionMediaTracks = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(mediaTrackDTO.peerConnectionId);
        Assertions.assertTrue(peerConnectionMediaTracks.contains(mediaTrackDTO.trackId));
    }
}