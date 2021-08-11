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
import java.time.Instant;
import java.util.*;

@Prototype
public class RemoveSfuTransportsTask extends ChainedTask<List<SfuEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveSfuTransportsTask.class);

    private Set<UUID> sfuTransportIds = new HashSet<>();
    private Map<UUID, SfuTransportDTO> removedSfuTransportDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
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
                .<Set<UUID>, Map<UUID, SfuTransportDTO>> addFunctionalStage("Remove Sfu Transport DTOs",
                        // action
                        identifiers -> {
                            identifiers.forEach(id -> {
                                if (this.removedSfuTransportDTOs.containsKey(id)) {
                                    return;
                                }
                                SfuTransportDTO DTO = this.hazelcastMaps.getSFUTransports().remove(id);
                                this.removedSfuTransportDTOs.put(DTO.transportId, DTO);
                            });
                            return this.removedSfuTransportDTOs;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getSFUTransports().putAll(this.removedSfuTransportDTOs);
                        })
                .<List<SfuEventReport.Builder>> addTerminalFunction("Completed", sfuEventBuildersObj -> {
                    var sfuEventBuilders = (List<SfuEventReport.Builder>) sfuEventBuildersObj;
                    List<SfuEventReport.Builder> result = new LinkedList<>();
                    this.removedSfuTransportDTOs.values().stream()
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

    public RemoveSfuTransportsTask whereSfuIds(Set<UUID> callIds) {
        if (Objects.isNull(callIds) || callIds.size() < 1) {
            return this;
        }
        this.sfuTransportIds.addAll(callIds);
        return this;
    }

    public RemoveSfuTransportsTask addRemovedSfuDTO(SfuTransportDTO DTO) {
        if (Objects.isNull(DTO)) {
            return this;
        }
        this.sfuTransportIds.add(DTO.transportId);
        this.removedSfuTransportDTOs.put(DTO.transportId, DTO);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuTransportDTO sfuTransportDTO) {
        Long now = Instant.now().toEpochMilli();
        try {
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_TRANSPORT_CLOSED.name())
                    .setTimestamp(now);
            return setupBuilder(builder, sfuTransportDTO);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }

    private SfuEventReport.Builder setupBuilder(SfuEventReport.Builder builder, SfuTransportDTO sfuTransportDTO) {
        try {
            return builder
                    .setMediaUnitId(sfuTransportDTO.mediaUnitId)
                    .setSfuId(sfuTransportDTO.sfuId.toString())
                    .setTransportId(sfuTransportDTO.transportId.toString())
                    ;
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }
}
