package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.evaluators.listeners.attachments.SfuTransportAttachment;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Prototype
class SfuTransportOpened  extends EventReporterAbstract.SfuEventReporterAbstract<SfuTransportDTO> {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportOpened.class);

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedSfuTransports()
                .subscribe(this::receiveAddedSfuTransport);

    }

    private void receiveAddedSfuTransport(List<SfuTransportDTO> sfuTransportDTOs) {
        if (Objects.isNull(sfuTransportDTOs) || sfuTransportDTOs.size() < 1) {
            return;
        }
        sfuTransportDTOs.stream()
                .map(this::makeReport)
                .forEach(this::forward);
    }

    private SfuEventReport makeReport(SfuTransportDTO sfuTransportDTO) {
        return this.makeReport(sfuTransportDTO, sfuTransportDTO.opened);
    }

    @Override
    protected SfuEventReport makeReport(SfuTransportDTO sfuTransportDTO, Long timestamp) {
        try {
            String sfuId = UUIDAdapter.toStringOrNull(sfuTransportDTO.sfuId);
            String callId = UUIDAdapter.toStringOrNull(sfuTransportDTO.callId);
            String transportId = UUIDAdapter.toStringOrNull(sfuTransportDTO.transportId);
            var attachment = SfuTransportAttachment.builder()
                    .withInternal(sfuTransportDTO.internal)
                    .build().toBase64();
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_TRANSPORT_OPENED.name())
                    .setSfuId(sfuId)
                    .setCallId(callId)
                    .setTransportId(transportId)
                    .setMessage("Sfu Transport is opened")
                    .setServiceId(sfuTransportDTO.serviceId)
                    .setMediaUnitId(sfuTransportDTO.mediaUnitId)
                    .setTimestamp(sfuTransportDTO.opened)
                    .setAttachments(attachment)
                    ;
            logger.info("SFU Transport (id: {}, internal: {}) is OPENED (mediaUnitId: {}, serviceId {})",
                    transportId, sfuTransportDTO.internal, sfuTransportDTO.mediaUnitId, sfuTransportDTO.serviceId
            );
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu Transport DTO", ex);
            return null;
        }
    }

}
