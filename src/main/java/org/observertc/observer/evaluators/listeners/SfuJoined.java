package org.observertc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
class SfuJoined extends EventReporterAbstract.SfuEventReporterAbstract {

    private static final Logger logger = LoggerFactory.getLogger(SfuJoined.class);

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedSfu()
                .subscribe(this::receiveSfuJoined);

    }

    private void receiveSfuJoined(List<SfuDTO> sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.size() < 1) {
            return;
        }
        var reports = sfuDTOs.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        this.forward(reports);
    }

    private SfuEventReport makeReport(SfuDTO sfuDTO) {
        return this.makeReport(sfuDTO, sfuDTO.joined);
    }

    protected SfuEventReport makeReport(SfuDTO sfuDTO, Long timestamp) {
        try {
            String sfuId = UUIDAdapter.toStringOrNull(sfuDTO.sfuId);
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_JOINED.name())
                    .setSfuId(sfuId)
                    .setMessage("Sfu is joined")
                    .setServiceId(sfuDTO.serviceId)
                    .setMediaUnitId(sfuDTO.mediaUnitId)
                    .setTimestamp(timestamp);
            logger.info("SFU (sfuId: {}, mediaUnitId: {}) is JOINED serviceId {}.", sfuId, sfuDTO.serviceId, sfuDTO.mediaUnitId);
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu DTO", ex);
            return null;
        }
    }
}
