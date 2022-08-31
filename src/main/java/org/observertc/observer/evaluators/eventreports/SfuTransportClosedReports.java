package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.evaluators.eventreports.attachments.SfuTransportAttachment;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class SfuTransportClosedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportClosedReports.class);

    @Inject
    BeanProvider<RemoveSfuTransportsTask> removeSfuTransportTask;

    @PostConstruct
    void setup() {

    }

    public List<SfuEventReport> mapRemovedSfuTransport(List<SfuTransportDTO> sfuTransportDTOs) {
        if (Objects.isNull(sfuTransportDTOs) || sfuTransportDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var reports = sfuTransportDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    public List<SfuEventReport> mapExpiredSfuTransport(List<RepositoryExpiredEvent<SfuTransportDTO>> expiredSfuTransports) {
        if (Objects.isNull(expiredSfuTransports) || expiredSfuTransports.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var task = this.removeSfuTransportTask.get();
        Map<UUID, Long> estimatedRemovals = new HashMap<>();
        expiredSfuTransports.stream().forEach(expiredSfuTransport -> {
            var sfuTransportDTO = expiredSfuTransport.getValue();
            var estimatedRemoval = expiredSfuTransport.estimatedLastTouch();
            estimatedRemovals.put(sfuTransportDTO.transportId, estimatedRemoval);
            task.addRemovedSfuTransportDTO(sfuTransportDTO);
        });

        if (!task.execute().succeeded()) {
            logger.warn("Removing expired SfuRtpPad was unsuccessful");
            return Collections.EMPTY_LIST;
        }

        var reports = task.getResult().stream().map(removedSfuTransport -> {
            Long estimatedRemoval = estimatedRemovals.getOrDefault(removedSfuTransport.transportId, Instant.now().toEpochMilli());
            var report = this.makeReport(removedSfuTransport, estimatedRemoval);
            return report;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return reports;
    }

    private SfuEventReport makeReport(SfuTransportDTO sfuTransportDTO) {
        var now = Instant.now().toEpochMilli();
        return this.makeReport(sfuTransportDTO, now);
    }

    private SfuEventReport makeReport(SfuTransportDTO sfuTransportDTO, Long timestamp) {
        try {
            String sfuId = UUIDAdapter.toStringOrNull(sfuTransportDTO.sfuId);
//            String callId = UUIDAdapter.toStringOrNull(sfuTransportDTO.callId);
            String transportId = UUIDAdapter.toStringOrNull(sfuTransportDTO.transportId);
            var attachment = SfuTransportAttachment.builder()
                    .withInternal(sfuTransportDTO.internal)
                    .build().toBase64();
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_TRANSPORT_CLOSED.name())
                    .setSfuId(sfuId)
//                    .setCallId(callId)
                    .setTransportId(transportId)
                    .setMessage("Sfu Transport is closed")
                    .setServiceId(sfuTransportDTO.serviceId)
                    .setMediaUnitId(sfuTransportDTO.mediaUnitId)
                    .setTimestamp(timestamp)
                    .setAttachments(attachment)
                    .setMarker(sfuTransportDTO.marker)
                    ;
            logger.info("SFU Transport (id: {}, internal: {}) is CLOSED (mediaUnitId: {}, serviceId {})",
                    transportId, sfuTransportDTO.internal, sfuTransportDTO.mediaUnitId, sfuTransportDTO.serviceId
            );
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu Transport DTO", ex);
            return null;
        }
    }
}
