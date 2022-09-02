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

    private CallEventReport makeReport(Models.InboundTrack inboundTrackModel) {
        try {
            var streamDirection = StreamDirection.INBOUND.name();
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(inboundTrackModel.getSfuStreamId())
                    .withSfuSinkId(inboundTrackModel.getSfuSinkId())
                    .withStreamDirection(streamDirection)
                    .withMediaKind(inboundTrackModel.getKind())
                    .build();
            String message = String.format("Media Track is added. streamId: %s, sinkId: %s, direction: %s, kind: %s", inboundTrackModel.getSfuStreamId(), inboundTrackModel.getSfuSinkId(), streamDirection, inboundTrackModel.getKind());
            var ssrc = inboundTrackModel.getSsrcCount() != 1 ? null : inboundTrackModel.getSsrc(0);
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_ADDED.name())
                    .setCallId(inboundTrackModel.getCallId())
                    .setServiceId(inboundTrackModel.getServiceId())
                    .setRoomId(inboundTrackModel.getRoomId())
                    .setClientId(inboundTrackModel.getClientId())
                    .setMediaUnitId(inboundTrackModel.getMediaUnitId())
                    .setUserId(inboundTrackModel.getUserId())
                    .setSSRC(ssrc)
                    .setPeerConnectionId(inboundTrackModel.getPeerConnectionId())
                    .setMediaTrackId(inboundTrackModel.getTrackId())
                    .setAttachments(attachment.toBase64())
                    .setTimestamp(inboundTrackModel.getAdded())
                    .setMarker(inboundTrackModel.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Media track {}, kind: {} (sfuStreamId: {}, sfuSinkId: {}) is ADDED on Peer Connection {} at call \"{}\" in service \"{}\" at room \"{}\"",
                    inboundTrackModel.getTrackId(), inboundTrackModel.getKind(), inboundTrackModel.getSfuStreamId(), inboundTrackModel.getSfuSinkId(), inboundTrackModel.getPeerConnectionId(), inboundTrackModel.getCallId(), inboundTrackModel.getServiceId(), inboundTrackModel.getRoomId());
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
