package org.observertc.observer.components.eventreports;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.repositories.tasks.RemoveSFUsTask;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class SfuLeftReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuLeftReports.class);

    @Inject
    BeanProvider<RemoveSFUsTask> removeSfuTask;

    @PostConstruct
    void setup() {

    }

    public List<SfuEventReport> mapExpiredSfuDTOs(List<RepositoryExpiredEvent<SfuDTO>> expiredSfuDTOs) {
        if (Objects.isNull(expiredSfuDTOs) || expiredSfuDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var task = this.removeSfuTask.get();
        Map<UUID, Long> estimatedRemovals = new HashMap<>();
        expiredSfuDTOs.stream().forEach(expiredSfuTransport -> {
            var sfuDTO = expiredSfuTransport.getValue();
            var estimatedRemoval = expiredSfuTransport.estimatedLastTouch();
            estimatedRemovals.put(sfuDTO.sfuId, estimatedRemoval);
            task.addRemovedSfuDTO(sfuDTO);
        });

        if (!task.execute().succeeded()) {
            logger.warn("Removing expired SfuLeft was unsuccessful");
            return Collections.EMPTY_LIST;
        }
        var reports = task.getResult().stream().map(removedSfu -> {
            Long estimatedRemoval = estimatedRemovals.getOrDefault(removedSfu.sfuId, Instant.now().toEpochMilli());
            var report = this.makeReport(removedSfu, estimatedRemoval);
            return report;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return reports;
    }

    public List<SfuEventReport> mapRemovedSfuDTOs(List<SfuDTO> sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var reports = sfuDTOs.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        return reports;
    }

    private SfuEventReport makeReport(SfuDTO sfuDTO) {
        Long now = Instant.now().toEpochMilli();
        return this.makeReport(sfuDTO, now);
    }

    protected SfuEventReport makeReport(SfuDTO sfuDTO, Long timestamp) {
        try {
            String sfuId = UUIDAdapter.toStringOrNull(sfuDTO.sfuId);
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_LEFT.name())
                    .setSfuId(sfuId)
                    .setMessage("Sfu is left")
                    .setServiceId(sfuDTO.serviceId)
                    .setMediaUnitId(sfuDTO.mediaUnitId)
                    .setTimestamp(timestamp)
                    .setMarker(sfuDTO.marker)
                    ;
            logger.info("SFU (sfuId: {}, mediaUnitId: {}) is LEFT. serviceId: {}.", sfuId, sfuDTO.serviceId, sfuDTO.mediaUnitId);
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu DTO", ex);
            return null;
        }
    }
}
