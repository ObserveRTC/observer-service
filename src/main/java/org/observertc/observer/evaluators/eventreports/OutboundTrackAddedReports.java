package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.MediaKind;
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
public class OutboundTrackAddedReports {

    private static final Logger logger = LoggerFactory.getLogger(OutboundTrackAddedReports.class);

    private Subject<List<CallEventReport>> output = PublishSubject.<List<CallEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.OutboundTrack> outboundAudioTracks) {
        if (Objects.isNull(outboundAudioTracks) || outboundAudioTracks.size() < 1) {
            return;
        }

        var reports = outboundAudioTracks.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private CallEventReport makeReport(Models.OutboundTrack outboundTrackModel) {
        try {
            var streamDirection = StreamDirection.OUTBOUND.name();
            MediaTrackAttachment attachment = MediaTrackAttachment.builder()
                    .withSfuStreamId(outboundTrackModel.getSfuStreamId())
                    .withStreamDirection(streamDirection)
                    .withMediaKind(MediaKind.AUDIO.name())
                    .build();
            String message = String.format("Media Track is added. streamId: %s, direction: %s", outboundTrackModel.getSfuStreamId(), streamDirection);
            var ssrc = outboundTrackModel.getSsrcCount() != 1 ? null : outboundTrackModel.getSsrc(0);
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_ADDED.name())
                    .setCallId(outboundTrackModel.getCallId())
                    .setServiceId(outboundTrackModel.getServiceId())
                    .setRoomId(outboundTrackModel.getRoomId())
                    .setClientId(outboundTrackModel.getClientId())
                    .setMediaUnitId(outboundTrackModel.getMediaUnitId())
                    .setUserId(outboundTrackModel.getUserId())
                    .setSSRC(ssrc)
                    .setPeerConnectionId(outboundTrackModel.getPeerConnectionId())
                    .setMediaTrackId(outboundTrackModel.getTrackId())
                    .setAttachments(attachment.toBase64())
                    .setTimestamp(outboundTrackModel.getAdded())
                    .setMarker(outboundTrackModel.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Media track {} (sfuStreamId: {}) is ADDED on Peer Connection {} at call \"{}\" in service \"{}\" at room \"{}\"",
                    outboundTrackModel.getTrackId(), outboundTrackModel.getSfuStreamId(), outboundTrackModel.getPeerConnectionId(), outboundTrackModel.getCallId(), outboundTrackModel.getServiceId(), outboundTrackModel.getRoomId());
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
