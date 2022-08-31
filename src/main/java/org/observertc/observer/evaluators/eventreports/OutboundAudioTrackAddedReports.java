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
public class OutboundAudioTrackAddedReports {

    private static final Logger logger = LoggerFactory.getLogger(OutboundAudioTrackAddedReports.class);

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapAddedOutboundAudioTrack(List<Models.OutboundAudioTrack> outboundAudioTracks) {
        if (Objects.isNull(outboundAudioTracks) || outboundAudioTracks.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var reports = outboundAudioTracks.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    private CallEventReport makeReport(Models.OutboundAudioTrack outboundAudioTrack) {
        try {
            var streamDirection = StreamDirection.OUTBOUND.name();
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(outboundAudioTrack.getSfuStreamId())
                    .withStreamDirection(streamDirection)
                    .withMediaKind(MediaKind.AUDIO.name())
                    .build();
            String message = String.format("Media Track is added. streamId: %s, direction: %s", outboundAudioTrack.getSfuStreamId(), streamDirection);
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_ADDED.name())
                    .setCallId(outboundAudioTrack.getCallId())
                    .setServiceId(outboundAudioTrack.getServiceId())
                    .setRoomId(outboundAudioTrack.getRoomId())
                    .setClientId(outboundAudioTrack.getClientId())
                    .setMediaUnitId(outboundAudioTrack.getMediaUnitId())
                    .setUserId(outboundAudioTrack.getUserId())
//                    .setSSRC(outboundAudioTrack.getS)
                    .setPeerConnectionId(outboundAudioTrack.getPeerConnectionId())
                    .setMediaTrackId(outboundAudioTrack.getTrackId())
                    .setAttachments(attachment.toBase64())
                    .setTimestamp(outboundAudioTrack.getAdded())
                    .setMarker(outboundAudioTrack.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Media track {} (sfuStreamId: {}) is ADDED on Peer Connection {} at call \"{}\" in service \"{}\" at room \"{}\"",
                    outboundAudioTrack.getTrackId(), outboundAudioTrack.getSfuStreamId(), outboundAudioTrack.getPeerConnectionId(), outboundAudioTrack.getCallId(), outboundAudioTrack.getServiceId(), outboundAudioTrack.getRoomId());
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
