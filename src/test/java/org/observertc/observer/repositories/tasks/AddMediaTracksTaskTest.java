package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.utils.DTOGenerators;
import org.observertc.observer.utils.TestUtils;

import java.util.Map;
import java.util.UUID;

@MicronautTest
class AddMediaTracksTaskTest {

    @Inject
    BeanProvider<AddMediaTracksTask> addMediaTracksTaskProvider;

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var expected = generator.getMediaTrackDTO();
        var task = addMediaTracksTaskProvider.get()
                .withMediaTrackDTOs(Map.of(expected.trackId, expected));

        task.execute();

        var actual = this.hazelcastMaps.getMediaTracks().get(expected.trackId);
        var equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void boundInboundTrackToPeerConnection_1() {
        var mediaTrackDTO = generator.getMediaTrackDTOBuilder()
                .withDirection(StreamDirection.INBOUND)
                .build();
        var task = addMediaTracksTaskProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var inboundMediaTrackIds = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(mediaTrackDTO.peerConnectionId);
        var contains = inboundMediaTrackIds.contains(mediaTrackDTO.trackId);
        Assertions.assertTrue(contains);
    }

    @Test
    public void boundOutboundTrackToPeerConnection_1() {
        var mediaTrackDTO = generator.getMediaTrackDTOBuilder()
                .withDirection(StreamDirection.OUTBOUND)
                .build();
        var task = addMediaTracksTaskProvider.get()
                .withMediaTrackDTOs(Map.of(mediaTrackDTO.trackId, mediaTrackDTO));

        task.execute();

        var outboundMediaTrackIds = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(mediaTrackDTO.peerConnectionId);
        var contains = outboundMediaTrackIds.contains(mediaTrackDTO.trackId);
        Assertions.assertTrue(contains);
    }

    @Test
    public void notCrashed_1() {
        var trackId = UUID.randomUUID();
        var task = addMediaTracksTaskProvider.get()
                .withMediaTrackDTOs(TestUtils.nullValuedMap(trackId));

        task.execute();

        var actual = this.hazelcastMaps.getMediaTracks().get(trackId);
        Assertions.assertNull(actual);
    }
}