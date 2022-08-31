package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.MediaKind;
import org.observertc.observer.common.StreamDirection;
import org.observertc.observer.evaluators.eventreports.attachments.MediaTrackAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.PeerConnectionsRepository;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class InboundAudioTrackAddedReports {

    private static final Logger logger = LoggerFactory.getLogger(InboundAudioTrackAddedReports.class);

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapAddedInboundAudioTrack(List<Models.InboundAudioTrack> inboundAudioTracks) {
        if (Objects.isNull(inboundAudioTracks) || inboundAudioTracks.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var reports = inboundAudioTracks.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    private CallEventReport makeReport(Models.InboundAudioTrack inboundAudioTrack) {
        try {
            var streamDirection = StreamDirection.INBOUND.name();
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(inboundAudioTrack.getSfuStreamId())
                    .withSfuSinkId(inboundAudioTrack.getSfuSinkId())
                    .withStreamDirection(streamDirection)
                    .withMediaKind(MediaKind.AUDIO.name())
                    .build();
            String message = String.format("Media Track is added. streamId: %s, sinkId: %s, direction: %s", inboundAudioTrack.getSfuStreamId(), inboundAudioTrack.getSfuSinkId(), streamDirection);
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_ADDED.name())
                    .setCallId(inboundAudioTrack.getCallId())
                    .setServiceId(inboundAudioTrack.getServiceId())
                    .setRoomId(inboundAudioTrack.getRoomId())
                    .setClientId(inboundAudioTrack.getClientId())
                    .setMediaUnitId(inboundAudioTrack.getMediaUnitId())
                    .setUserId(inboundAudioTrack.getUserId())
//                    .setSSRC(inboundAudioTrack.getS)
                    .setPeerConnectionId(inboundAudioTrack.getPeerConnectionId())
                    .setMediaTrackId(inboundAudioTrack.getTrackId())
                    .setAttachments(attachment.toBase64())
                    .setTimestamp(inboundAudioTrack.getAdded())
                    .setMarker(inboundAudioTrack.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Media track {} (sfuStreamId: {}, sfuSinkId: {}) is ADDED on Peer Connection {} at call \"{}\" in service \"{}\" at room \"{}\"",
                    inboundAudioTrack.getTrackId(), inboundAudioTrack.getSfuStreamId(), inboundAudioTrack.getSfuSinkId(), inboundAudioTrack.getPeerConnectionId(), inboundAudioTrack.getCallId(), inboundAudioTrack.getServiceId(), inboundAudioTrack.getRoomId());
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
