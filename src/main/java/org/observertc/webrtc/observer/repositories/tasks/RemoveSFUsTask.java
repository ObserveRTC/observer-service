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
import java.time.Instant;
import java.util.*;

@Prototype
public class RemoveSFUsTask extends ChainedTask<List<SfuEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveSFUsTask.class);

    private Set<UUID> sfuIds = new HashSet<>();
    private Map<UUID, SfuDTO> removedSfuDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
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
                .<Set<UUID>, Map<UUID, SfuDTO>> addFunctionalStage("Remove Sfu DTOs",
                        // action
                        sfuIds -> {
                            sfuIds.forEach(sfuId -> {
                                if (this.removedSfuDTOs.containsKey(sfuId)) {
                                    return;
                                }
                                SfuDTO sfuDTO = this.hazelcastMaps.getSFUs().remove(sfuId);
                                this.removedSfuDTOs.put(sfuId, sfuDTO);
                            });
                            return this.removedSfuDTOs;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getSFUs().putAll(this.removedSfuDTOs);
                        })
                .<List<SfuEventReport.Builder>> addTerminalFunction("Completed", sfuEventBuildersObj -> {
                    var sfuEventBuilders = (List<SfuEventReport.Builder>) sfuEventBuildersObj;
                    List<SfuEventReport.Builder> result = new LinkedList<>();
                    this.removedSfuDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .forEach(result::add);
                    if (Objects.nonNull(sfuEventBuilders)) {
                        sfuEventBuilders.stream().forEach(result::add);
                    }
                    return result;
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

    private SfuEventReport.Builder makeReportBuilder(SfuDTO sfuDTO) {
        Long now = Instant.now().toEpochMilli();
        try {
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_LEFT.name())
                    .setTimestamp(now);
            return setupBuilder(builder, sfuDTO);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }

    private SfuEventReport.Builder setupBuilder(SfuEventReport.Builder builder, SfuDTO sfuDTO) {
        try {
            return builder
                    .setMediaUnitId(sfuDTO.mediaUnitId)
                    .setSfuId(sfuDTO.sfuId.toString())
                    ;
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }
}
