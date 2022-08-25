package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HamokStorages;
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
    HamokStorages hamokStorages;

    @Inject
    RepositoryMetrics exposedMetrics;

    @Inject
    BeanProvider<RemoveSfuRtpPadsTask> removeSfuRtpPadsTaskProvider;

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
                                SfuTransportDTO sfuTransportDTO = this.hamokStorages.getSFUTransports().remove(id);
                                if (Objects.isNull(sfuTransportDTO)) {
                                    logger.debug("Not found SfuTransportDTO for transportId {}. Perhaps it was ejected before it was ordered to be removed.", id);
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
                            this.hamokStorages.getSFUTransports().putAll(this.removedSfuTransportDTOs);
                        })
                .addActionStage("Remove Sfu to SfuTransport bindings", () -> {
                    this.removedSfuTransportDTOs.values().forEach(sfuTransportDTO -> {
                        if (sfuTransportDTO.transportId == null || sfuTransportDTO.sfuId == null) return;
                        this.hamokStorages.getSfuToSfuTransportIds().remove(sfuTransportDTO.sfuId, sfuTransportDTO.transportId);
                    });
                },
                // rollback
                (something, thrownException) -> {
                    this.removedSfuTransportDTOs.values().forEach(sfuTransportDTO -> {
                        if (sfuTransportDTO.transportId == null || sfuTransportDTO.sfuId == null) return;
                        this.hamokStorages.getSfuToSfuTransportIds().put(sfuTransportDTO.sfuId, sfuTransportDTO.transportId);
                    });
                })
                .addActionStage("Remove Sfu Rtp Pad Entities",
                        () -> {
                            Set<UUID> allRtpPadIds = new HashSet<>();
                            this.removedSfuTransportDTOs.keySet().forEach(sfuTransportId -> {
                                Collection<UUID> rtpPadIds = this.hamokStorages.getSfuTransportToSfuRtpPadIds().get(sfuTransportId);
                                if (Objects.nonNull(rtpPadIds)) {
                                    rtpPadIds.forEach(rtpPadId -> {
                                        allRtpPadIds.add(rtpPadId);
                                    });
                                }
                            });
                            if (allRtpPadIds.size() < 1) {
                                return;
                            }
                            var task = this.removeSfuRtpPadsTaskProvider.get();
                            task.whereSfuRtpStreamPadIds(allRtpPadIds);

                            if (!task.execute().succeeded()) {
                                logger.warn("Remove RtpPad failed");
                                return;
                            }
                        }
                )
                .addTerminalSupplier("Completed", () -> {
                    return this.removedSfuTransportDTOs.values().stream().collect(Collectors.toList());
                })
                .build();
    }

    public RemoveSfuTransportsTask whereSfuTransportIds(Set<UUID> sfuTransportIds) {
        if (Objects.isNull(sfuTransportIds) || sfuTransportIds.size() < 1) {
            return this;
        }
        sfuTransportIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.sfuTransportIds::add);
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
