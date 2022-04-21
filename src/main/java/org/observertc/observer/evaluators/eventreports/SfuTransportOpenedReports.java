package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.evaluators.eventreports.attachments.SfuTransportAttachment;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.events.SfuEventType;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class SfuTransportOpenedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportOpenedReports.class);

    @PostConstruct
    void setup() {

    }

    public List<SfuEventReport> mapAddedSfuTransport(List<SfuTransportDTO> sfuTransportDTOs) {
        if (Objects.isNull(sfuTransportDTOs) || sfuTransportDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var reports = sfuTransportDTOs.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        return reports;
    }


    private SfuEventReport makeReport(SfuTransportDTO sfuTransportDTO) {
        try {
            String sfuId = UUIDAdapter.toStringOrNull(sfuTransportDTO.sfuId);
//            String callId = UUIDAdapter.toStringOrNull(sfuTransportDTO.callId);
            String transportId = UUIDAdapter.toStringOrNull(sfuTransportDTO.transportId);
            var attachment = SfuTransportAttachment.builder()
                    .withInternal(sfuTransportDTO.internal)
                    .build().toBase64();
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_TRANSPORT_OPENED.name())
                    .setSfuId(sfuId)
//                    .setCallId(callId)
                    .setTransportId(transportId)
                    .setMessage("Sfu Transport is opened")
                    .setServiceId(sfuTransportDTO.serviceId)
                    .setMediaUnitId(sfuTransportDTO.mediaUnitId)
                    .setTimestamp(sfuTransportDTO.opened)
                    .setAttachments(attachment)
                    .setMarker(sfuTransportDTO.marker)
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
