package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.SfuDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemoveSFUsTask extends ChainedTask<List<SfuDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveSFUsTask.class);

    private Set<UUID> sfuIds = new HashSet<>();
    private Map<UUID, SfuDTO> removedSfuDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
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
                .addTerminalSupplier("Completed", () -> {
                    return this.removedSfuDTOs.values().stream().collect(Collectors.toList());
                })
                .build();
    }

    public RemoveSFUsTask whereSfuIds(Set<UUID> callIds) {
        if (Objects.isNull(callIds) || callIds.size() < 1) {
            return this;
        }
        this.sfuIds.addAll(callIds);
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
