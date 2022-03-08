package org.observertc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.evaluators.listeners.attachments.MediaTrackAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Prototype
class MediaTrackAdded extends EventReporterAbstract.CallEventReporterAbstract<MediaTrackDTO> {

    private static final Logger logger = LoggerFactory.getLogger(MediaTrackAdded.class);

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedMediaTracks()
                .subscribe(this::receiveAddedMediaTracks);

    }

    private void receiveAddedMediaTracks(List<MediaTrackDTO> mediaTrackDTOs) {
        if (Objects.isNull(mediaTrackDTOs) || mediaTrackDTOs.size() < 1) {
            return;
        }

        mediaTrackDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private CallEventReport makeReport(MediaTrackDTO mediaTrackDTO) {
        Long now = Instant.now().toEpochMilli();
        return this.makeReport(mediaTrackDTO, mediaTrackDTO.added);
    }

    @Override
    protected CallEventReport makeReport(MediaTrackDTO mediaTrackDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(mediaTrackDTO.callId);
            String clientId = UUIDAdapter.toStringOrNull(mediaTrackDTO.clientId);
            String peerConnectionId = UUIDAdapter.toStringOrNull(mediaTrackDTO.peerConnectionId);
            String trackId = UUIDAdapter.toStringOrNull(mediaTrackDTO.trackId);
            String rtpStreamId = UUIDAdapter.toStringOrNull(mediaTrackDTO.rtpStreamId);
            String streamDirection = mediaTrackDTO.direction != null ? mediaTrackDTO.direction.name() : null;
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withRtpStreamId(rtpStreamId)
                    .withStreamDirection(streamDirection)
                    .build();
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_ADDED.name())
                    .setCallId(callId)
                    .setServiceId(mediaTrackDTO.serviceId)
                    .setRoomId(mediaTrackDTO.roomId)
                    .setClientId(clientId)
                    .setMediaUnitId(mediaTrackDTO.mediaUnitId)
                    .setUserId(mediaTrackDTO.userId)
                    .setSSRC(mediaTrackDTO.ssrc)
                    .setPeerConnectionId(peerConnectionId)
                    .setMediaTrackId(trackId)
                    .setAttachments(attachment.toBase64())
                    .setTimestamp(timestamp)
                    .build();
            logger.info("Media track {} is added on Peer Connection {} at call \"{}\" in service \"{}\" at room \"{}\". Direction: {}", mediaTrackDTO.trackId, mediaTrackDTO.peerConnectionId, mediaTrackDTO.callId, mediaTrackDTO.serviceId, mediaTrackDTO.roomId, mediaTrackDTO.direction);
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
