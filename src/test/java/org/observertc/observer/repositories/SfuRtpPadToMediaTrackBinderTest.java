package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.utils.DTOGenerators;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class SfuRtpPadToMediaTrackBinderTest {

    @Inject
    HamokStorages hamokStorages;

    @Inject
    SfuRtpPadToMediaTrackBinder sfuRtpPadToMediaTrackBinder;

    @Inject
    RepositoryEvents repositoryEvents;

    DTOGenerators dtoGenerators = new DTOGenerators();

    @Test
    void shouldMakeSfuStream() throws ExecutionException, InterruptedException, TimeoutException {
        var inboundRtpPad = dtoGenerators.getSfuRtpPadDTOBuilder()
                .withStreamDirection(StreamDirection.INBOUND)
                .withSinkId(null)
                .build();
        var mediaTrack = dtoGenerators.getMediaTrackDTOBuilder()
                .withDirection(StreamDirection.OUTBOUND)
                .withSfuStreamId(inboundRtpPad.streamId)
                .withSfuSinkId(null)
                .build();

        var sfuStreams = new CompletableFuture<List<RepositoryUpdatedEvent<SfuStreamDTO>>>();
        repositoryEvents.updatedSfuStreams().subscribe(sfuStreams::complete);
        sfuRtpPadToMediaTrackBinder.onSfuRtpPadsAdded(List.of(inboundRtpPad));
        sfuRtpPadToMediaTrackBinder.onMediaTracksAdded(List.of(mediaTrack));

        var addedSfuStream = sfuStreams.get(10, TimeUnit.SECONDS).get(0).getNewValue();
        Assertions.assertEquals(inboundRtpPad.streamId, addedSfuStream.sfuStreamId);
        Assertions.assertEquals(inboundRtpPad.transportId, addedSfuStream.sfuTransportId);
        Assertions.assertEquals(inboundRtpPad.sfuId, addedSfuStream.sfuId);

        Assertions.assertEquals(mediaTrack.trackId, addedSfuStream.trackId);
        Assertions.assertEquals(mediaTrack.peerConnectionId, addedSfuStream.peerConnectionId);
        Assertions.assertEquals(mediaTrack.clientId, addedSfuStream.clientId);

        Assertions.assertEquals(mediaTrack.callId, addedSfuStream.callId);
    }

    @Test
    void shouldMakeSfuSink() throws ExecutionException, InterruptedException, TimeoutException {
        var sinkId = UUID.randomUUID();
        var inboundRtpPad = dtoGenerators.getSfuRtpPadDTOBuilder()
                .withStreamDirection(StreamDirection.OUTBOUND)
                .withSinkId(sinkId)
                .build();
        var mediaTrack = dtoGenerators.getMediaTrackDTOBuilder()
                .withDirection(StreamDirection.INBOUND)
                .withSfuStreamId(inboundRtpPad.streamId)
                .withSfuSinkId(sinkId)
                .build();

        var sfuSinks = new CompletableFuture<List<RepositoryUpdatedEvent<SfuSinkDTO>>>();
        repositoryEvents.updatedSuSinks().subscribe(sfuSinks::complete);
        sfuRtpPadToMediaTrackBinder.onSfuRtpPadsAdded(List.of(inboundRtpPad));
        sfuRtpPadToMediaTrackBinder.onMediaTracksAdded(List.of(mediaTrack));

        var addedSfuStream = sfuSinks.get(10, TimeUnit.SECONDS).get(0).getNewValue();
        Assertions.assertEquals(inboundRtpPad.streamId, addedSfuStream.sfuStreamId);
        Assertions.assertEquals(inboundRtpPad.sinkId, addedSfuStream.sfuSinkId);
        Assertions.assertEquals(inboundRtpPad.transportId, addedSfuStream.sfuTransportId);
        Assertions.assertEquals(inboundRtpPad.sfuId, addedSfuStream.sfuId);

        Assertions.assertEquals(mediaTrack.trackId, addedSfuStream.trackId);
        Assertions.assertEquals(mediaTrack.peerConnectionId, addedSfuStream.peerConnectionId);
        Assertions.assertEquals(mediaTrack.clientId, addedSfuStream.clientId);

        Assertions.assertEquals(mediaTrack.callId, addedSfuStream.callId);
    }
}