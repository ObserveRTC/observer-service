package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemoveSfuTransportsTask extends ChainedTask<List<SfuTransportDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveSfuTransportsTask.class);

    private Set<UUID> sfuTransportIds = new HashSet<>();
    private Map<UUID, SfuTransportDTO> removedSfuTransportDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<List<SfuTransportDTO>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Fetch SfuTransport Ids",
                        () -> this.sfuTransportIds,
                        receivedIds -> {
                            this.sfuTransportIds.addAll(receivedIds);
                            return this.sfuTransportIds;
                        }
                )
                .<Set<UUID>>addBreakCondition((Ids, resultHolder) -> {
                    if (Objects.isNull(Ids)) {
                        this.getLogger().warn("No Entities have been passed");
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    if (Ids.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .<Set<UUID>> addConsumerStage("Remove Sfu Transport DTOs",
                        // action
                        identifiers -> {
                            identifiers.forEach(id -> {
                                if (this.removedSfuTransportDTOs.containsKey(id)) {
                                    return;
                                }
                                SfuTransportDTO sfuTransportDTO = this.hazelcastMaps.getSFUTransports().remove(id);
                                if (Objects.isNull(sfuTransportDTO)) {
                                    logger.warn("Not found SfuTransportDTO for transportId {}", id);
                                    return;
                                }
                                this.removedSfuTransportDTOs.put(sfuTransportDTO.transportId, sfuTransportDTO);
                            });
                            return;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getSFUTransports().putAll(this.removedSfuTransportDTOs);
                        })
                .addTerminalSupplier("Completed", () -> {
                    return this.removedSfuTransportDTOs.values().stream().collect(Collectors.toList());
                })
                .build();
    }

    public RemoveSfuTransportsTask whereSfuTransportIds(Set<UUID> sfuTransportIds) {
        if (Objects.isNull(sfuTransportIds) || sfuTransportIds.size() < 1) {
            return this;
        }
        sfuTransportIds.stream().filter(Utils::nonNull).forEach(this.sfuTransportIds::add);
        return this;
    }

    public RemoveSfuTransportsTask addRemovedSfuTransportDTO(SfuTransportDTO DTO) {
        if (Objects.isNull(DTO)) {
            return this;
        }
        this.sfuTransportIds.add(DTO.transportId);
        this.removedSfuTransportDTOs.put(DTO.transportId, DTO);
        return this;
    }
}
