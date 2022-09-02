package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
import org.observertc.observer.evaluators.eventreports.attachments.SfuTransportAttachment;
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
public class SfuTransportClosedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportClosedReports.class);

    private Subject<List<SfuEventReport>> output = PublishSubject.<List<SfuEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.SfuTransport> sfuTransportDTOs) {
        if (Objects.isNull(sfuTransportDTOs) || sfuTransportDTOs.size() < 1) {
            return;
        }
        var reports = sfuTransportDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private SfuEventReport makeReport(Models.SfuTransport sfuTransportDTO) {
        try {
            var timestamp = sfuTransportDTO.hasTouched() ? sfuTransportDTO.getTouched() : Instant.now().toEpochMilli();
            var attachment = SfuTransportAttachment.builder()
                    .withInternal(sfuTransportDTO.getInternal())
                    .build().toBase64();
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_TRANSPORT_CLOSED.name())
                    .setSfuId(sfuTransportDTO.getTransportId())
//                    .setCallId(callId)
                    .setTransportId(sfuTransportDTO.getTransportId())
                    .setMessage("Sfu Transport is closed")
                    .setServiceId(sfuTransportDTO.getServiceId())
                    .setMediaUnitId(sfuTransportDTO.getMediaUnitId())
                    .setTimestamp(timestamp)
                    .setAttachments(attachment)
                    .setMarker(sfuTransportDTO.getMarker())
                    ;
            logger.info("SFU Transport (id: {}, internal: {}) is CLOSED (mediaUnitId: {}, serviceId {})",
                    sfuTransportDTO.getTransportId(),
                    sfuTransportDTO.getInternal(),
                    sfuTransportDTO.getMediaUnitId(),
                    sfuTransportDTO.getServiceId()
            );
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu Transport DTO", ex);
            return null;
        }
    }

    public Observable<List<SfuEventReport>> getOutput() {
        return this.output;
    }
}
