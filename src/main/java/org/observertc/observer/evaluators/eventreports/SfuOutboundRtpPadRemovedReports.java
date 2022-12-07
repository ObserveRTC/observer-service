package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.StreamDirection;
import org.observertc.observer.evaluators.eventreports.attachments.RtpPadAttachment;
import org.observertc.observer.events.SfuEventType;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class SfuOutboundRtpPadRemovedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuOutboundRtpPadRemovedReports.class);

    private Subject<List<SfuEventReport>> output = PublishSubject.<List<SfuEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.SfuOutboundRtpPad> trackModels) {
        if (Objects.isNull(trackModels) || trackModels.size() < 1) {
            return;
        }
        var reports = trackModels.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private SfuEventReport makeReport(Models.SfuOutboundRtpPad trackModel) {
        try {
            var timestamp = trackModel.hasSampleTouched() ? trackModel.getSampleTouched() : Instant.now().toEpochMilli();
            var streamDirection = StreamDirection.OUTBOUND;
            var attachment = RtpPadAttachment.builder()
                    .withStreamDirection(streamDirection)
                    .withInternal(trackModel.getInternal())
                    .build().toBase64();
            var sfuStreamId = trackModel.hasSfuStreamId() ? trackModel.getSfuStreamId() : null;
            var sfuSinkId = trackModel.hasSfuSinkId() ? trackModel.getSfuSinkId() : null;
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_REMOVED.name())
                    .setSfuId(trackModel.getSfuId())
//                    .setCallId(callId)
                    .setTransportId(trackModel.getSfuTransportId())
                    .setMediaStreamId(sfuStreamId)
                    .setMediaSinkId(sfuSinkId)
                    .setRtpPadId(trackModel.getRtpPadId())
                    .setAttachments(attachment)
                    .setMessage("Sfu Rtp Pad is removed")
                    .setServiceId(trackModel.getServiceId())
                    .setMediaUnitId(trackModel.getMediaUnitId())
                    .setTimestamp(timestamp)
                    .setMarker(trackModel.getMarker())
                    ;
            logger.info("SFU Pad (id: {}, streamId: {}, sinkId: {}) is ADDED (mediaUnitId: {}, serviceId {}), direction is {}",
                    trackModel.getRtpPadId(),
                    sfuStreamId,
                    sfuSinkId,
                    trackModel.getMediaUnitId(),
                    trackModel.getServiceId(),
                    streamDirection
            );
            return builder.build();
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }

    }

    public Observable<List<SfuEventReport>> getOutput() {
        return this.output;
    }
}
