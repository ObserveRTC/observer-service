package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class AddSFUsTask extends ChainedTask<List<SfuEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(AddSFUsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Map<UUID, SfuDTO> sfuDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
                .<Map<UUID, SfuDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedSfuDTOs -> {
                            if (Objects.nonNull(receivedSfuDTOs)) {
                                this.sfuDTOs.putAll(receivedSfuDTOs);
                            }
                        }
                )
                .<Map<UUID, SfuDTO>> addBreakCondition((resultHolder) -> {
                    if (this.sfuDTOs.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add Sfu DTOs",
                // action
                () -> {
                    hazelcastMaps.getSFUs().putAll(this.sfuDTOs);
                },
                // rollback
                (inputHolder, thrownException) -> {
                    for (UUID sfuId : this.sfuDTOs.keySet()) {
                        this.hazelcastMaps.getSFUs().remove(sfuId);
                    }
                })
                .addTerminalSupplier("Completed", () -> {
                    List<SfuEventReport.Builder> result = this.sfuDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
    }

    public AddSFUsTask withSfuDTO(SfuDTO sfuDTO) {
        if (Objects.isNull(sfuDTO)) {
            this.getLogger().info("sfu uuid was not given to be added");
            return this;
        }
        this.sfuDTOs.put(sfuDTO.sfuId, sfuDTO);
        return this;
    }

    public AddSFUsTask withSfuDTOs(SfuDTO... sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.length < 1) {
            this.getLogger().info("sfu uuid was not given to be added");
            return this;
        }
        Arrays.stream(sfuDTOs).forEach(sfuDTO -> {
            this.sfuDTOs.put(sfuDTO.sfuId, sfuDTO);
        });
        return this;
    }

    public AddSFUsTask withSfuDTOs(Map<UUID, SfuDTO> sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.size() < 1) {
            this.getLogger().info("sfu uuid was not given to be added");
            return this;
        }
        this.sfuDTOs.putAll(sfuDTOs);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuDTO sfuDTO) {
        try {
            return SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_JOINED.name())
                    .setSfuName(sfuDTO.sfuName)
                    .setSfuId(sfuDTO.sfuId.toString())
                    .setMediaUnitId(sfuDTO.mediaUnitId)
                    .setTimestamp(sfuDTO.joined);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for Sfu DTO", ex);
            return null;
        }
    }
}
