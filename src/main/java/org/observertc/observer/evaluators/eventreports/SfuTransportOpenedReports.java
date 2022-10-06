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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class SfuTransportOpenedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportOpenedReports.class);

    private Subject<List<SfuEventReport>> output = PublishSubject.<List<SfuEventReport>>create().toSerialized();


    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.SfuTransport> transportModels) {
        if (Objects.isNull(transportModels) || transportModels.size() < 1) {
            return;
        }
        var reports = transportModels.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }


    private SfuEventReport makeReport(Models.SfuTransport sfuTransportDTO) {
        try {
            var attachment = SfuTransportAttachment.builder()
                    .withInternal(sfuTransportDTO.getInternal())
                    .build().toBase64();
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_TRANSPORT_OPENED.name())
                    .setSfuId(sfuTransportDTO.getSfuId())
//                    .setCallId(callId)
                    .setTransportId(sfuTransportDTO.getTransportId())
                    .setMessage("Sfu Transport is opened")
                    .setServiceId(sfuTransportDTO.getServiceId())
                    .setMediaUnitId(sfuTransportDTO.getMediaUnitId())
                    .setTimestamp(sfuTransportDTO.getOpened())
                    .setAttachments(attachment)
                    .setMarker(sfuTransportDTO.getMarker())
                    ;
            logger.info("SFU Transport (id: {}, internal: {}) is OPENED (mediaUnitId: {}, serviceId {})",
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
