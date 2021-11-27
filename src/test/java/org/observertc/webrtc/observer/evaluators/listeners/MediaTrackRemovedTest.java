package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.evaluators.listeners.attachments.MediaTrackAttachment;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.utils.DTOGenerators;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class MediaTrackRemovedTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    MediaTrackRemoved mediaTrackRemoved;

    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var mediaTrackDTO = dtoGenerators.getMediaTrackDTO();

        this.mediaTrackRemoved.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        this.hazelcastMaps.getMediaTracks().remove(mediaTrackDTO.trackId);
        var callEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);
        var timestampThreshold = Instant.now().plusSeconds(5).toEpochMilli();

        Assertions.assertEquals(CallEventType.MEDIA_TRACK_REMOVED.name(), callEventReport.getName());
        Assertions.assertEquals(mediaTrackDTO.callId.toString(), callEventReport.getCallId());
        Assertions.assertEquals(mediaTrackDTO.serviceId, callEventReport.getServiceId());
        Assertions.assertEquals(mediaTrackDTO.roomId, callEventReport.getRoomId());

        Assertions.assertEquals(mediaTrackDTO.clientId.toString(), callEventReport.getClientId());
        Assertions.assertEquals(mediaTrackDTO.mediaUnitId, callEventReport.getMediaUnitId());
        Assertions.assertEquals(mediaTrackDTO.userId, callEventReport.getUserId());


        Assertions.assertEquals(mediaTrackDTO.peerConnectionId.toString(), callEventReport.getPeerConnectionId());
        Assertions.assertEquals(mediaTrackDTO.trackId.toString(), callEventReport.getMediaTrackId());
        Assertions.assertEquals(mediaTrackDTO.ssrc, callEventReport.getSSRC());
        Assertions.assertTrue(callEventReport.getTimestamp() < timestampThreshold);
    }

    @Test
    void reportAttachmentFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var mediaTrackDTO = dtoGenerators.getMediaTrackDTO();

        this.mediaTrackRemoved.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        this.hazelcastMaps.getMediaTracks().remove(mediaTrackDTO.trackId);

        String rtpStreamId = UUIDAdapter.toStringOrNull(mediaTrackDTO.rtpStreamId);
        String attachmentInBase64 = callEventReportPromise.get(5, TimeUnit.SECONDS).getAttachments();
        MediaTrackAttachment attachment = MediaTrackAttachment.builder().fromBase64(attachmentInBase64).build();
        Assertions.assertEquals(rtpStreamId, attachment.rtpStreamId);
        Assertions.assertEquals(mediaTrackDTO.direction.name(), attachment.streamDirection);
    }

    @Test
    void ifFieldsForAttachmentIsNullNoProblemOccurs() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var mediaTrackDTO = MediaTrackDTO.builder().from(dtoGenerators.getMediaTrackDTO())
                .withRtpStreamId(null)
                .build();

        this.mediaTrackRemoved.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        this.hazelcastMaps.getMediaTracks().remove(mediaTrackDTO.trackId);

        String rtpStreamId = UUIDAdapter.toStringOrNull(mediaTrackDTO.rtpStreamId);
        String attachmentInBase64 = callEventReportPromise.get(5, TimeUnit.SECONDS).getAttachments();
        MediaTrackAttachment attachment = MediaTrackAttachment.builder().fromBase64(attachmentInBase64).build();
        Assertions.assertEquals(rtpStreamId, attachment.rtpStreamId);
        Assertions.assertEquals(mediaTrackDTO.direction.name(), attachment.streamDirection);
    }
}