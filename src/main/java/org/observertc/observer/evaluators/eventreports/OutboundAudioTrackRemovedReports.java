package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.MediaKind;
import org.observertc.observer.common.StreamDirection;
import org.observertc.observer.evaluators.eventreports.attachments.MediaTrackAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.PeerConnection;
import org.observertc.observer.repositories.PeerConnectionsRepository;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class OutboundAudioTrackRemovedReports {

    private static final Logger logger = LoggerFactory.getLogger(OutboundAudioTrackRemovedReports.class);

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @PostConstruct
    void setup() {
    }

    public List<CallEventReport> mapRemovedOutboundAudioTrack(List<Models.OutboundAudioTrack> peerConnectionModels) {
        if (Objects.isNull(peerConnectionModels) || peerConnectionModels.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var peerConnectionIds = peerConnectionModels.stream().map(Models.OutboundAudioTrack::getPeerConnectionId)
                .collect(Collectors.toSet());
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionIds);
        var reports = new LinkedList<CallEventReport>();
        var now = Instant.now().toEpochMilli();
        for (var mediaTrackModel : peerConnectionModels) {
            var peerConnection = peerConnections.get(mediaTrackModel.getPeerConnectionId());
            var report = this.perform(peerConnection, mediaTrackModel, now);
            if (report != null) {
                reports.add(report);
            }
        }
        this.peerConnectionsRepository.save();
        return reports;
    }

    public List<CallEventReport> mapExpiredOutboundAudioTrack(List<RepositoryExpiredEvent<Models.OutboundAudioTrack>> expiredPeerConnectionDTOs) {
        if (Objects.isNull(expiredPeerConnectionDTOs) || expiredPeerConnectionDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var peerConnectionIds = expiredPeerConnectionDTOs.stream().map(event -> event.getValue().getPeerConnectionId())
                .collect(Collectors.toSet());
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionIds);
        var reports = new LinkedList<CallEventReport>();
        for (var event : expiredPeerConnectionDTOs) {
            var mediaTrackModel = event.getValue();
            var peerConnection = peerConnections.get(mediaTrackModel.getPeerConnectionId());
            var report = this.perform(peerConnection, mediaTrackModel, event.estimatedLastTouch());
            if (report != null) {
                reports.add(report);
            }
        }
        this.peerConnectionsRepository.save();
        return reports;
    }

    private CallEventReport perform(PeerConnection peerConnection, Models.OutboundAudioTrack outboundAudioTrack, Long timestamp) {
        if (peerConnection == null) {
            logger.warn("Did not found peerConnection for outbound audio track {}", outboundAudioTrack.getTrackId());
            return null;
        }
        var removed = peerConnection.removeOutboundAudioTrack(outboundAudioTrack.getTrackId());
        if (!removed) {
            logger.warn("Did not removed outbound audio track {} from peer connection {}. Already removed?", outboundAudioTrack.getTrackId(), outboundAudioTrack.getPeerConnectionId());
            return null;
        }
        return this.makeReport(outboundAudioTrack, timestamp);
    }

    private CallEventReport makeReport(Models.OutboundAudioTrack mediaTrackDTO, Long timestamp) {
        try {
            var streamDirection = StreamDirection.OUTBOUND.name();
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(mediaTrackDTO.getSfuStreamId())
                    .withStreamDirection(streamDirection)
                    .withMediaKind(MediaKind.AUDIO.name())
                    .build();
            String message = String.format("Media Track is removed. sfuStreamId: %s, sfuSinkId: %s, direction: %s", mediaTrackDTO.getSfuStreamId(), streamDirection);
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_REMOVED.name())
                    .setCallId(mediaTrackDTO.getCallId())
                    .setServiceId(mediaTrackDTO.getServiceId())
                    .setRoomId(mediaTrackDTO.getRoomId())
                    .setClientId(mediaTrackDTO.getClientId())
                    .setMediaUnitId(mediaTrackDTO.getMediaUnitId())
                    .setUserId(mediaTrackDTO.getUserId())
//                    .setSSRC(mediaTrackDTO.ssrc)
                    .setPeerConnectionId(mediaTrackDTO.getPeerConnectionId())
                    .setMediaTrackId(mediaTrackDTO.getTrackId())
                    .setAttachments(attachment.toBase64())
                    .setTimestamp(timestamp)
                    .setMarker(mediaTrackDTO.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Media Track {} (sfuStreamId: {}) on Peer Connection {} is REMOVED at call \"{}\" in service \"{}\" at room \"{}\"",
                    mediaTrackDTO.getTrackId(),
                    mediaTrackDTO.getSfuStreamId(),
                    mediaTrackDTO.getPeerConnectionId(),
                    mediaTrackDTO.getCallId(),
                    mediaTrackDTO.getServiceId(),
                    mediaTrackDTO.getRoomId());
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
