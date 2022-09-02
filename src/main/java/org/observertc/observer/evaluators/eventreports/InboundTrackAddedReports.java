package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.StreamDirection;
import org.observertc.observer.evaluators.eventreports.attachments.MediaTrackAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class InboundTrackAddedReports {

    private static final Logger logger = LoggerFactory.getLogger(InboundTrackAddedReports.class);

    private Subject<List<CallEventReport>> output = PublishSubject.<List<CallEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.InboundTrack> inboundTracks) {
        if (Objects.isNull(inboundTracks) || inboundTracks.size() < 1) {
            return;
        }

        var reports = inboundTracks.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private CallEventReport makeReport(Models.InboundTrack inboundTrack) {
        try {
            var streamDirection = StreamDirection.INBOUND.name();
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(inboundTrack.getSfuStreamId())
                    .withSfuSinkId(inboundTrack.getSfuSinkId())
                    .withStreamDirection(streamDirection)
                    .withMediaKind(inboundTrack.getKind())
                    .build();
            String message = String.format("Media Track is added. streamId: %s, sinkId: %s, direction: %s, kind: %s", inboundTrack.getSfuStreamId(), inboundTrack.getSfuSinkId(), streamDirection, inboundTrack.getKind());
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_ADDED.name())
                    .setCallId(inboundTrack.getCallId())
                    .setServiceId(inboundTrack.getServiceId())
                    .setRoomId(inboundTrack.getRoomId())
                    .setClientId(inboundTrack.getClientId())
                    .setMediaUnitId(inboundTrack.getMediaUnitId())
                    .setUserId(inboundTrack.getUserId())
//                    .setSSRC(inboundAudioTrack.getS)
                    .setPeerConnectionId(inboundTrack.getPeerConnectionId())
                    .setMediaTrackId(inboundTrack.getTrackId())
                    .setAttachments(attachment.toBase64())
                    .setTimestamp(inboundTrack.getAdded())
                    .setMarker(inboundTrack.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Media track {}, kind: {} (sfuStreamId: {}, sfuSinkId: {}) is ADDED on Peer Connection {} at call \"{}\" in service \"{}\" at room \"{}\"",
                    inboundTrack.getTrackId(), inboundTrack.getKind(), inboundTrack.getSfuStreamId(), inboundTrack.getSfuSinkId(), inboundTrack.getPeerConnectionId(), inboundTrack.getCallId(), inboundTrack.getServiceId(), inboundTrack.getRoomId());
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }

    public Observable<List<CallEventReport>> getOutput() {
        return this.output;
    }
}
