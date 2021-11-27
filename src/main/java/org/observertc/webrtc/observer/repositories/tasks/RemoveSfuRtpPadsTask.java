package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemoveSfuRtpPadsTask extends ChainedTask<List<SfuRtpPadDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveSfuRtpPadsTask.class);

    private Set<UUID> padIds = new HashSet<>();
    private Map<UUID, SfuRtpPadDTO> removedRtpPads = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<List<SfuRtpPadDTO>>(this)
                .<Set<UUID>>addConsumerEntry("Fetch SfuTransport Ids",
                        () -> {},
                        receivedIds -> {
                            this.padIds.addAll(receivedIds);
                        }
                )
                .addBreakCondition(resultHolder -> {
                    if (this.padIds.size() < 1 && this.removedRtpPads.size() < 1) {
                        this.getLogger().warn("No Entities have been passed");
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Remove Sfu Rtp Stream Pad DTOs",
                        // action
                        () -> {
                            this.padIds.forEach(id -> {
                                if (this.removedRtpPads.containsKey(id)) {
                                    return;
                                }
                                SfuRtpPadDTO DTO = this.hazelcastMaps.getSFURtpPads().remove(id);
                                this.removedRtpPads.put(DTO.sfuPadId, DTO);
                            });
                            return;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getSFURtpPads().putAll(this.removedRtpPads);
                        })
                .addActionStage("Remove Bindings RtpPads to rtp streamIds",
                        // action
                        () -> {
                            this.removedRtpPads.forEach((padId, sfuRtpPadDTO) -> {
                                this.hazelcastMaps.getRtpStreamIdToSfuPadIds().remove(sfuRtpPadDTO.rtpStreamId, padId);
                            });
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.removedRtpPads.forEach((padId, sfuRtpPadDTO) -> {
                                this.hazelcastMaps.getRtpStreamIdToSfuPadIds().put(sfuRtpPadDTO.rtpStreamId, padId);
                            });
                        })
                .addTerminalSupplier("Completed", () -> {
                    return this.removedRtpPads.values().stream().collect(Collectors.toList());
//                    List<SfuEventReport.Builder> result = new LinkedList<>();
//                    this.removedRtpPads.values().stream()
//                            .map(this::makeReportBuilder)
//                            .filter(Objects::nonNull)
//                            .forEach(result::add);
//                    return result;
                })
                .build();
    }

    public RemoveSfuRtpPadsTask whereSfuRtpStreamPodIds(Set<UUID> podIds) {
        if (Objects.isNull(podIds) || podIds.size() < 1) {
            return this;
        }
        this.padIds.addAll(podIds);
        return this;
    }

    public RemoveSfuRtpPadsTask addRemovedSfuRtpStreamPadDTO(SfuRtpPadDTO DTO) {
        if (Objects.isNull(DTO)) {
            return this;
        }
        this.padIds.add(DTO.sfuPadId);
        this.removedRtpPads.put(DTO.sfuPadId, DTO);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuRtpPadDTO sfuRtpPadDTO) {
        Long now = Instant.now().toEpochMilli();
        try {
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_REMOVED.name())
                    .setTimestamp(now);
            return setupBuilder(builder, sfuRtpPadDTO);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }

    private SfuEventReport.Builder setupBuilder(SfuEventReport.Builder builder, SfuRtpPadDTO sfuRtpPadDTO) {
        try {
            return builder
                    .setMediaUnitId(sfuRtpPadDTO.mediaUnitId)
                    .setSfuId(sfuRtpPadDTO.sfuId.toString())
                    .setTransportId(sfuRtpPadDTO.sfuTransportId.toString())
                    .setRtpStreamId(sfuRtpPadDTO.rtpStreamId.toString())
                    ;
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }
}
