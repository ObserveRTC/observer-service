package org.observertc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.evaluators.listeners.attachments.MediaTrackAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.repositories.tasks.RemoveMediaTracksTask;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Prototype
class MediaTrackRemoved extends EventReporterAbstract.CallEventReporterAbstract<MediaTrackDTO> {

    private static final Logger logger = LoggerFactory.getLogger(MediaTrackRemoved.class);

    @Inject
    Provider<RemoveMediaTracksTask> removeMediaTracksTaskProvider;

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .removedMediaTracks()
                .subscribe(this::receiveRemovedMediaTracks);

        this.repositoryEvents
                .expiredMediaTracks()
                .subscribe(this::receiveExpiredMediaTracks);
    }

    private void receiveRemovedMediaTracks(List<MediaTrackDTO> mediaTrackDTOs) {
        if (Objects.isNull(mediaTrackDTOs) || mediaTrackDTOs.size() < 1) {
            return;
        }

        mediaTrackDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private void receiveExpiredMediaTracks(List<RepositoryExpiredEvent<MediaTrackDTO>> expiredMediaTracks) {
        if (Objects.isNull(expiredMediaTracks) || expiredMediaTracks.size() < 1) {
            return;
        }
        var removePeerConnectionsTask = removeMediaTracksTaskProvider.get();
        expiredMediaTracks.stream().map(expiredDTO -> expiredDTO.getValue()).forEach(removePeerConnectionsTask::addremovedMediaTrackDTO);
        if (!removePeerConnectionsTask.execute().succeeded()) {
            logger.warn("Remove Peer Connection are failed");
            return;
        }

        expiredMediaTracks.stream()
                .filter(Objects::nonNull)
                .map(expiredMediaTrackDTO -> {
                    var timestamp = expiredMediaTrackDTO.estimatedLastTouch();
                    var mediaTrackDTO = expiredMediaTrackDTO.getValue();
                    if (Objects.isNull(mediaTrackDTO) || Objects.isNull(mediaTrackDTO.peerConnectionId)) {
                        return null;
                    }
                    var report = this.makeReport(mediaTrackDTO, timestamp);
                    return report;
                })
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private CallEventReport makeReport(MediaTrackDTO mediaTrackDTO) {
        Long now = Instant.now().toEpochMilli();
        return this.makeReport(mediaTrackDTO, now);
    }

    @Override
    protected CallEventReport makeReport(MediaTrackDTO mediaTrackDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(mediaTrackDTO.callId);
            String clientId = UUIDAdapter.toStringOrNull(mediaTrackDTO.clientId);
            String peerConnectionId = UUIDAdapter.toStringOrNull(mediaTrackDTO.peerConnectionId);
            String trackId = UUIDAdapter.toStringOrNull(mediaTrackDTO.trackId);
            String sfuStreamId = UUIDAdapter.toStringOrNull(mediaTrackDTO.sfuStreamId);
            String streamDirection = mediaTrackDTO.direction != null ? mediaTrackDTO.direction.name() : null;
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(sfuStreamId)
                    .withStreamDirection(streamDirection)
                    .build();
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_REMOVED.name())
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
            logger.info("Media Track {} on Peer Connection {} is closed at call \"{}\" in service \"{}\" at room \"{}\". Direction: {}", mediaTrackDTO.trackId, mediaTrackDTO.peerConnectionId, mediaTrackDTO.callId, mediaTrackDTO.serviceId, mediaTrackDTO.roomId, mediaTrackDTO.direction);
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
