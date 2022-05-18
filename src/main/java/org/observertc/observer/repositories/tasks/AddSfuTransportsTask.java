package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Prototype
public class AddSfuTransportsTask extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddSfuTransportsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    private Map<UUID, SfuTransportDTO> sfuTransports = new HashMap<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
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
                        resultHolder.set(null);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add Sfu Transport DTOs",
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
                .addActionStage("Bind SFU Transport Ids to SFU",
                        // action
                        () -> {
                            this.sfuTransports.values().stream()
                                    .filter(Objects::nonNull)
                                    .forEach(sfuTransportDTO -> {
                                        this.hazelcastMaps.getSfuToSfuTransportIds().put(sfuTransportDTO.sfuId, sfuTransportDTO.transportId);
                                    });

                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.sfuTransports.values().stream()
                                    .filter(Objects::nonNull)
                                    .forEach(sfuTransportDTO -> {
                                        this.hazelcastMaps.getSfuToSfuTransportIds().remove(sfuTransportDTO.sfuId, sfuTransportDTO.transportId);
                                    });
                        })
                .addTerminalPassingStage("Completed")
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

    public AddSfuTransportsTask withSfuTransportDTOs(Map<UUID, SfuTransportDTO> sfuTransportDTOs) {
        if (Objects.isNull(sfuTransportDTOs) || sfuTransportDTOs.size() < 1) {
            this.getLogger().info("sfu transport DTO was not given to be added");
            return this;
        }
        sfuTransportDTOs.values().stream().filter(Utils::expensiveNonNullCheck).forEach(sfuTransportDTO -> {
            this.sfuTransports.put(sfuTransportDTO.transportId, sfuTransportDTO);
        });
        return this;
    }
}
