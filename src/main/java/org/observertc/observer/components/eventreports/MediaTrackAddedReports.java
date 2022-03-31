package org.observertc.observer.components.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.components.eventreports.attachments.MediaTrackAttachment;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class MediaTrackAddedReports {

    private static final Logger logger = LoggerFactory.getLogger(MediaTrackAddedReports.class);

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapAddedMediaTracks(List<MediaTrackDTO> mediaTrackDTOs) {
        if (Objects.isNull(mediaTrackDTOs) || mediaTrackDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var reports = mediaTrackDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    private CallEventReport makeReport(MediaTrackDTO mediaTrackDTO) {
        try {
            String callId = UUIDAdapter.toStringOrNull(mediaTrackDTO.callId);
            String clientId = UUIDAdapter.toStringOrNull(mediaTrackDTO.clientId);
            String peerConnectionId = UUIDAdapter.toStringOrNull(mediaTrackDTO.peerConnectionId);
            String trackId = UUIDAdapter.toStringOrNull(mediaTrackDTO.trackId);
            String sfuStreamId = UUIDAdapter.toStringOrNull(mediaTrackDTO.sfuStreamId);
            String sfuSinkId = UUIDAdapter.toStringOrNull(mediaTrackDTO.sfuSinkId);
            String streamDirection = mediaTrackDTO.direction != null ? mediaTrackDTO.direction.name() : null;
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(sfuStreamId)
                    .withSfuSinkId(sfuSinkId)
                    .withStreamDirection(streamDirection)
                    .build();
            String message = String.format("Media Track is added. streamId: %s, sinkId: %s, direction: %s", sfuStreamId, sfuSinkId, streamDirection);
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
                    .setTimestamp(mediaTrackDTO.added)
                    .setMarker(mediaTrackDTO.marker)
                    .setMessage(message)
                    .build();
            logger.info("Media track {} (sfuStreamId: {}, sfuSinkId: {}) is ADDED on Peer Connection {} at call \"{}\" in service \"{}\" at room \"{}\". Direction: {}",
                    mediaTrackDTO.trackId, mediaTrackDTO.sfuStreamId, mediaTrackDTO.sfuSinkId, mediaTrackDTO.peerConnectionId, mediaTrackDTO.callId, mediaTrackDTO.serviceId, mediaTrackDTO.roomId, mediaTrackDTO.direction);
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
