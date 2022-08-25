package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HamokStorages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemoveSFUsTask extends ChainedTask<List<SfuDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveSFUsTask.class);

    private Set<UUID> sfuIds = new HashSet<>();
    private Map<UUID, SfuDTO> removedSfuDTOs = new HashMap<>();

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    @Inject
    BeanProvider<RemoveSfuTransportsTask> removeSfuTransportsTaskProvider;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<List<SfuDTO>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Fetch SfuIds",
                        () -> this.sfuIds,
                        receivedIds -> {
                            this.sfuIds.addAll(receivedIds);
                            return this.sfuIds;
                        }
                )
                .<Set<UUID>>addBreakCondition((sfuIds, resultHolder) -> {
                    if (Objects.isNull(sfuIds)) {
                        this.getLogger().warn("No Entities have been passed");
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    if (sfuIds.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .<Set<UUID>> addConsumerStage("Remove Sfu DTOs",
                        // action
                        sfuIds -> {
                            sfuIds.forEach(sfuId -> {
                                if (this.removedSfuDTOs.containsKey(sfuId)) {
                                    return;
                                }
                                SfuDTO sfuDTO = this.hazelcastMaps.getSFUs().remove(sfuId);
                                if (Objects.isNull(sfuDTO)) {
                                    logger.debug("Not found SfuDTO for sfuId {}. Perhaps it was ejected before it was ordered to be removed.", sfuId);
                                    return;
                                }
                                this.removedSfuDTOs.put(sfuId, sfuDTO);
                            });
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getSFUs().putAll(this.removedSfuDTOs);
                        })
                .addActionStage("Remove Sfu Transport Entities",
                        () -> {
                            Set<UUID> allSfuTransportIds = new HashSet<>();
                            this.removedSfuDTOs.keySet().forEach(clientId -> {
                                Collection<UUID> sfuTransportIds = this.hazelcastMaps.getSfuToSfuTransportIds().get(clientId);
                                if (Objects.nonNull(sfuTransportIds)) {
                                    sfuTransportIds.forEach(sfuTransportId -> {
                                        allSfuTransportIds.add(sfuTransportId);
                                    });
                                }
                            });
                            if (allSfuTransportIds.size() < 1) {
                                return;
                            }
                            var task = this.removeSfuTransportsTaskProvider.get();
                            task.whereSfuTransportIds(allSfuTransportIds);

                            if (!task.execute().succeeded()) {
                                logger.warn("Remove SFU Transports failed");
                                return;
                            }
                        }
                )
                .addTerminalSupplier("Completed", () -> {
                    return this.removedSfuDTOs.values().stream().collect(Collectors.toList());
                })
                .build();
    }

    public RemoveSFUsTask whereSfuIds(Set<UUID> sfuIds) {
        if (Objects.isNull(sfuIds) || sfuIds.size() < 1) {
            return this;
        }
        sfuIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.sfuIds::add);
        return this;
    }

    public RemoveSFUsTask addRemovedSfuDTO(SfuDTO sfuDTO) {
        if (Objects.isNull(sfuDTO)) {
            return this;
        }
        this.sfuIds.add(sfuDTO.sfuId);
        this.removedSfuDTOs.put(sfuDTO.sfuId, sfuDTO);
        return this;
    }
}
