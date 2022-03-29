package org.observertc.observer.components.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.components.eventreports.attachments.RtpPadAttachment;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.repositories.tasks.RemoveSfuRtpPadsTask;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Prototype
public class SfuRtpPadRemovedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadRemovedReports.class);

    @Inject
    Provider<RemoveSfuRtpPadsTask> removeSfuRtpPadsTaskProvider;

    @PostConstruct
    void setup() {

    }

    public List<SfuEventReport> mapRemovedSfuRtpPad(List<SfuRtpPadDTO> removedSfuRtpPads) {
        if (Objects.isNull(removedSfuRtpPads) || removedSfuRtpPads.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        Long now = Instant.now().toEpochMilli();
        var reports = new LinkedList<SfuEventReport>();
        for (var sfuRtpPad : removedSfuRtpPads) {
            var report = this.makeReport(sfuRtpPad, now);
            reports.add(report);
        }
        return reports;
    }

    public List<SfuEventReport> mapExpiredSfuRtpPad(List<RepositoryExpiredEvent<SfuRtpPadDTO>> expiredSfuRtpPads) {
        if (Objects.isNull(expiredSfuRtpPads) || expiredSfuRtpPads.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var reports = new LinkedList<SfuEventReport>();
        for (var expiredSfuRtpPad : expiredSfuRtpPads) {
            var timestamp = expiredSfuRtpPad.estimatedLastTouch();
            var sfuRtpPad = expiredSfuRtpPad.getValue();
            var report = this.makeReport(sfuRtpPad, timestamp);
            reports.add(report);
        }
        return reports;
    }

    private SfuEventReport makeReport(SfuRtpPadDTO sfuRtpPad, Long timestamp) {
        try {
            var attachment = RtpPadAttachment.builder()
                    .withStreamDirection(sfuRtpPad.streamDirection)
                    .withInternal(sfuRtpPad.internal)
                    .build().toBase64();
            String sfuPadId = UUIDAdapter.toStringOrNull(sfuRtpPad.rtpPadId);
            String sfuPadStreamDirection = Objects.nonNull(sfuRtpPad.streamDirection) ? sfuRtpPad.streamDirection.toString() : "Unknown";
            String streamId = UUIDAdapter.toStringOrNull(sfuRtpPad.streamId);
            String sinkId = UUIDAdapter.toStringOrNull(sfuRtpPad.sinkId);
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_REMOVED.name())
                    .setSfuId(sfuRtpPad.sfuId.toString())
//                    .setCallId(callId)
                    .setTransportId(sfuRtpPad.transportId.toString())
                    .setMediaStreamId(streamId)
                    .setMediaSinkId(sinkId)
                    .setRtpPadId(sfuPadId)
                    .setAttachments(attachment)
                    .setMessage("Sfu Rtp Pad is added")
                    .setServiceId(sfuRtpPad.serviceId)
                    .setMediaUnitId(sfuRtpPad.mediaUnitId)
                    .setTimestamp(timestamp)
                    .setMarker(sfuRtpPad.marker)
                    ;
            logger.info("SFU Pad (id: {}, streamId: {}, sinkId: {}) is REMOVED (mediaUnitId: {}, serviceId {}), direction is {}",
                    sfuPadId, sfuRtpPad.streamId, sfuRtpPad.sinkId, sfuRtpPad.mediaUnitId, sfuRtpPad.serviceId, sfuPadStreamDirection
            );
            return builder.build();
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
