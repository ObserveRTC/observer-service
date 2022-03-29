package org.observertc.observer.components.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.SfuDTO;
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
public class SfuJoinedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuJoinedReports.class);


    @PostConstruct
    void setup() {

    }

    public List<SfuEventReport> mapSfuDTOs(List<SfuDTO> sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var reports = sfuDTOs.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        return reports;
    }


    protected SfuEventReport makeReport(SfuDTO sfuDTO) {
        try {
            String sfuId = UUIDAdapter.toStringOrNull(sfuDTO.sfuId);
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_JOINED.name())
                    .setSfuId(sfuId)
                    .setMessage("Sfu is joined")
                    .setServiceId(sfuDTO.serviceId)
                    .setMediaUnitId(sfuDTO.mediaUnitId)
                    .setTimestamp(sfuDTO.joined)
                    .setMarker(sfuDTO.marker)
                    ;
            logger.info("SFU (sfuId: {}, mediaUnitId: {}) is JOINED serviceId {}.", sfuId, sfuDTO.serviceId, sfuDTO.mediaUnitId);
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu DTO", ex);
            return null;
        }
    }
}
