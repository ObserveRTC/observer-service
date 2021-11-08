package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.SfuDTO;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Prototype
class SfuJoined extends EventReporterAbstract.SfuEventReporterAbstract<SfuDTO> {

    private static final Logger logger = LoggerFactory.getLogger(SfuJoined.class);

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.bindListener(
                this.repositoryEvents.addedSfu(),
                this::receiveSfuJoined
        );
    }

    private void receiveSfuJoined(List<SfuDTO> sfuDTOs) {
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
