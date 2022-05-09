package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class AddSFUsTask extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddSFUsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    private Map<UUID, SfuDTO> sfuDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
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
                        resultHolder.set(null);
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
                .addTerminalPassingStage("Completed")
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
        Arrays.stream(sfuDTOs).filter(Utils::nonNull).forEach(sfuDTO -> {
            this.sfuDTOs.put(sfuDTO.sfuId, sfuDTO);
        });
        return this;
    }

    public AddSFUsTask withSfuDTOs(Map<UUID, SfuDTO> sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.size() < 1) {
            this.getLogger().info("sfu uuid was not given to be added");
            return this;
        }
        sfuDTOs.values().stream().filter(Utils::nonNull).forEach(sfuDTO -> {
            this.sfuDTOs.put(sfuDTO.sfuId, sfuDTO);
        });
        return this;
    }
}
