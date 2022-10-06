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
public class SfuInboundRtpPadRemovedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuInboundRtpPadRemovedReports.class);

    private Subject<List<SfuEventReport>> output = PublishSubject.<List<SfuEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.SfuInboundRtpPad> trackModels) {
        if (Objects.isNull(trackModels) || trackModels.size() < 1) {
            return;
        }
        var reports = trackModels.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private SfuEventReport makeReport(Models.SfuInboundRtpPad sfuInboundRtpPadModel) {
        try {
            var timestamp = sfuInboundRtpPadModel.hasTouched() ? sfuInboundRtpPadModel.getTouched() : Instant.now().toEpochMilli();
            var streamDirection = StreamDirection.INBOUND;
            var attachment = RtpPadAttachment.builder()
                    .withStreamDirection(streamDirection)
                    .withInternal(sfuInboundRtpPadModel.getInternal())
                    .build().toBase64();
            var sfuStreamId = sfuInboundRtpPadModel.hasSfuStreamId() ? sfuInboundRtpPadModel.getSfuStreamId() : null;
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_REMOVED.name())
                    .setSfuId(sfuInboundRtpPadModel.getSfuId())
//                    .setCallId(callId)
                    .setTransportId(sfuInboundRtpPadModel.getSfuTransportId())
                    .setMediaStreamId(sfuStreamId)
//                    .setMediaSinkId(trackModel)
                    .setRtpPadId(sfuInboundRtpPadModel.getRtpPadId())
                    .setAttachments(attachment)
                    .setMessage("Sfu Rtp Pad is removed")
                    .setServiceId(sfuInboundRtpPadModel.getServiceId())
                    .setMediaUnitId(sfuInboundRtpPadModel.getMediaUnitId())
                    .setTimestamp(timestamp)
                    .setMarker(sfuInboundRtpPadModel.getMarker())
                    ;
            logger.info("SFU Pad (id: {}, streamId: {}) is REMOVED (mediaUnitId: {}, serviceId {}), direction is {}",
                    sfuInboundRtpPadModel.getRtpPadId(),
                    sfuStreamId,
                    sfuInboundRtpPadModel.getMediaUnitId(),
                    sfuInboundRtpPadModel.getServiceId(),
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
