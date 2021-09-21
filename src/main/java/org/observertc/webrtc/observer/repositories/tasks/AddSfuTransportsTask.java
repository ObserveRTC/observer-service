package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class AddSfuTransportsTask extends ChainedTask<List<SfuEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(AddSfuTransportsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Map<UUID, SfuTransportDTO> sfuTransports = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
                .<Map<UUID, SfuTransportDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedSfuTransportDTOs -> {
                            if (Objects.nonNull(receivedSfuTransportDTOs)) {
                                this.sfuTransports.putAll(receivedSfuTransportDTOs);
                            }
                        }
                )
                .<Map<UUID, SfuTransportDTO>> addBreakCondition((resultHolder) -> {
                    if (this.sfuTransports.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add Sfu DTOs",
                // action
                () -> {
                    hazelcastMaps.getSFUTransports().putAll(this.sfuTransports);
                },
                // rollback
                (inputHolder, thrownException) -> {
                    for (UUID sfuId : this.sfuTransports.keySet()) {
                        this.hazelcastMaps.getSFUTransports().remove(sfuId);
                    }
                })
                .addTerminalSupplier("Completed", () -> {
                    List<SfuEventReport.Builder> result = this.sfuTransports.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
    }

    public AddSfuTransportsTask withSfuTransportDTO(SfuTransportDTO sfuTransportDTO) {
        if (Objects.isNull(sfuTransportDTO)) {
            this.getLogger().info("sfu transport DTO was not given to be added");
            return this;
        }
        this.sfuTransports.put(sfuTransportDTO.transportId, sfuTransportDTO);
        return this;
    }

    public AddSfuTransportsTask withSfuTransportDTOs(SfuTransportDTO... sfuTransportDTOs) {
        if (Objects.isNull(sfuTransportDTOs) || sfuTransportDTOs.length < 1) {
            this.getLogger().info("sfu transport DTO was not given to be added");
            return this;
        }
        Arrays.stream(sfuTransportDTOs).forEach(sfuTransportDTO -> {
            this.sfuTransports.put(sfuTransportDTO.transportId, sfuTransportDTO);
        });
        return this;
    }

    public AddSfuTransportsTask withSfuTransportDTOs(Map<UUID, SfuTransportDTO> sfuTransportDTOs) {
        if (Objects.isNull(sfuTransportDTOs) || sfuTransportDTOs.size() < 1) {
            this.getLogger().info("sfu transport DTO was not given to be added");
            return this;
        }
        this.sfuTransports.putAll(sfuTransportDTOs);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuTransportDTO sfuTransportDTO) {
        try {
            return SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_TRANSPORT_OPENED.name())
                    .setSfuName(sfuTransportDTO.sfuName)
                    .setSfuId(sfuTransportDTO.sfuId.toString())
                    .setTransportId(sfuTransportDTO.transportId.toString())
                    .setMediaUnitId(sfuTransportDTO.mediaUnitId)
                    .setTimestamp(sfuTransportDTO.opened);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for Sfu Transport DTO", ex);
            return null;
        }
    }
}
