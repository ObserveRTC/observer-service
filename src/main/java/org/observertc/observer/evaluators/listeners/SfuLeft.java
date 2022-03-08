package org.observertc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.repositories.tasks.RemoveSFUsTask;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.*;

@Prototype
class SfuLeft extends EventReporterAbstract.SfuEventReporterAbstract<SfuDTO> {

    private static final Logger logger = LoggerFactory.getLogger(SfuLeft.class);

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    Provider<RemoveSFUsTask> removeSfuTask;

    @PostConstruct
    void setup() {
        this.bindListener(
                this.repositoryEvents.removedSfu(),
                this::removedSfuJoined
        );

        this.bindListener(
                this.repositoryEvents.expiredSfu(),
                this::receiveExpiredSfu
        );

    }

    private void receiveExpiredSfu(List<RepositoryExpiredEvent<SfuDTO>> expiredSfuDTOs) {
        if (Objects.isNull(expiredSfuDTOs) || expiredSfuDTOs.size() < 1) {
            return;
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
            return;
        }
        task.getResult().stream().map(removedSfu -> {
            Long estimatedRemoval = estimatedRemovals.getOrDefault(removedSfu.sfuId, Instant.now().toEpochMilli());
            var report = this.makeReport(removedSfu, estimatedRemoval);
            return report;
        }).filter(Objects::nonNull).forEach(this::forward);
    }

    private void removedSfuJoined(List<SfuDTO> sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.size() < 1) {
            return;
        }
        sfuDTOs.stream()
                .map(this::makeReport)
                .forEach(this::forward);
    }

    private SfuEventReport makeReport(SfuDTO sfuDTO) {
        return this.makeReport(sfuDTO, sfuDTO.joined);
    }

    @Override
    protected SfuEventReport makeReport(SfuDTO sfuDTO, Long timestamp) {
        try {
            String sfuId = UUIDAdapter.toStringOrNull(sfuDTO.sfuId);
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_LEFT.name())
                    .setSfuId(sfuId)
                    .setMessage("Sfu is left")
                    .setServiceId(sfuDTO.serviceId)
                    .setMediaUnitId(sfuDTO.mediaUnitId)
                    .setTimestamp(timestamp);
            logger.info("SFU (sfuId: {}, mediaUnitId: {}) is LEFT serviceId {}.", sfuId, sfuDTO.serviceId, sfuDTO.mediaUnitId);
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu DTO", ex);
            return null;
        }
    }
}
