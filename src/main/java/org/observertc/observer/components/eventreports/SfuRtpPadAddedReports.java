package org.observertc.observer.components.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.components.eventreports.attachments.RtpPadAttachment;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.SfuRtpPadEvents;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Prototype
public class SfuRtpPadAddedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadAddedReports.class);

    @PostConstruct
    void setup() {

    }

    public List<SfuEventReport> mapCompletedSfuRtpPads(List<SfuRtpPadEvents.Payload> payloads) {
        if (Objects.isNull(payloads) || payloads.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var reports = new LinkedList<SfuEventReport>();
        for (var payload : payloads) {
            var report = this.makeReport(payload.sfuRtpPadDTO, payload.sfuStreamDTO, payload.sfuSinkDTO);
            reports.add(report);
        }
        return reports;
    }

    private SfuEventReport makeReport(SfuRtpPadDTO sfuRtpPad, SfuStreamDTO sfuStream, SfuSinkDTO sfuSink) {
        try {
            var attachment = RtpPadAttachment.builder()
                    .withStreamDirection(sfuRtpPad.streamDirection)
                    .withInternal(sfuRtpPad.internal)
                    .build().toBase64();
            String sfuPadId = UUIDAdapter.toStringOrNull(sfuRtpPad.rtpPadId);
            String sfuPadStreamDirection = Objects.nonNull(sfuRtpPad.streamDirection) ? sfuRtpPad.streamDirection.toString() : "Unknown";
            String callId = null;
            String streamId = null;
            String sinkId = null;
            if (Objects.nonNull(sfuSink)) {
                callId = UUIDAdapter.toStringOrNull(sfuSink.callId);
                sinkId = UUIDAdapter.toStringOrNull(sfuSink.sfuSinkId);
            } else if (Objects.nonNull(sfuStream)) {
                callId = UUIDAdapter.toStringOrNull(sfuStream.callId);
                streamId = UUIDAdapter.toStringOrNull(sfuSink.sfuStreamId);
            }

            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_ADDED.name())
                    .setSfuId(sfuRtpPad.sfuId.toString())
                    .setCallId(callId)
                    .setTransportId(sfuRtpPad.transportId.toString())
                    .setMediaStreamId(streamId)
                    .setMediaSinkId(sinkId)
                    .setRtpPadId(sfuPadId)
                    .setAttachments(attachment)
                    .setMessage("Sfu Rtp Pad is added")
                    .setServiceId(sfuRtpPad.serviceId)
                    .setMediaUnitId(sfuRtpPad.mediaUnitId)
                    .setTimestamp(sfuRtpPad.added);
            logger.info("SFU Pad (id: {}, streamId: {}, sinkId: {}) is ADDED (mediaUnitId: {}, serviceId {}), direction is {}",
                    sfuPadId, sfuRtpPad.streamId, sfuRtpPad.sinkId, sfuRtpPad.mediaUnitId, sfuRtpPad.serviceId, sfuPadStreamDirection
            );
            return builder.build();
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }

    protected SfuEventReport makeReport(SfuRtpPadDTO input, Long timestamp) {
        logger.warn("makeReport(SfuRtpPadDTO input, Long timestamp) is called, supposed not to");
        return null;
    }
}
