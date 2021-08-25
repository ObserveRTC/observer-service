package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuRtpStreamPodDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;

@Prototype
public class RemoveSfuRtpStreamsTask extends ChainedTask<List<SfuEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveSfuRtpStreamsTask.class);

    private Set<UUID> podIds = new HashSet<>();
    private Map<UUID, SfuRtpStreamPodDTO> removedRtpStreamPods = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
                .<Set<UUID>>addConsumerEntry("Fetch SfuTransport Ids",
                        () -> {},
                        receivedIds -> {
                            this.podIds.addAll(receivedIds);
                        }
                )
                .addBreakCondition(resultHolder -> {
                    if (this.podIds.size() < 1 && this.removedRtpStreamPods.size() < 1) {
                        this.getLogger().warn("No Entities have been passed");
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Remove Sfu Rtp Stream Pod DTOs",
                        // action
                        () -> {
                            this.podIds.forEach(id -> {
                                if (this.removedRtpStreamPods.containsKey(id)) {
                                    return;
                                }
                                SfuRtpStreamPodDTO DTO = this.hazelcastMaps.getSFURtpPods().remove(id);
                                this.removedRtpStreamPods.put(DTO.sfuPodId, DTO);
                            });
                            return;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getSFURtpPods().putAll(this.removedRtpStreamPods);
                        })
                .addActionStage("Remove Sfu Rtp Stream Relations",
                        // action
                        () -> {
                            this.removedRtpStreamPods.forEach((sfuPodId, sfuPodDTO) -> {
                                this.hazelcastMaps.getSfuStreamToRtpPodIds().remove(sfuPodDTO.sfuStreamId, sfuPodId);
                            });
                            return;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            this.removedRtpStreamPods.forEach((sfuPodId, sfuPodDTO) -> {
                                this.hazelcastMaps.getSfuStreamToRtpPodIds().put(sfuPodDTO.sfuStreamId, sfuPodId);
                            });
                        })
                .addTerminalSupplier("Completed", () -> {
                    List<SfuEventReport.Builder> result = new LinkedList<>();
                    this.removedRtpStreamPods.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .forEach(result::add);
                    return result;
                })
                .build();
    }

    public RemoveSfuRtpStreamsTask whereSfuRtpStreamPodIds(Set<UUID> podIds) {
        if (Objects.isNull(podIds) || podIds.size() < 1) {
            return this;
        }
        this.podIds.addAll(podIds);
        return this;
    }

    public RemoveSfuRtpStreamsTask addRemovedSfuRtpStreamPodDTO(SfuRtpStreamPodDTO DTO) {
        if (Objects.isNull(DTO)) {
            return this;
        }
        this.podIds.add(DTO.sfuPodId);
        this.removedRtpStreamPods.put(DTO.sfuPodId, DTO);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuRtpStreamPodDTO sfuRtpStreamPodDTO) {
        Long now = Instant.now().toEpochMilli();
        try {
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_STREAM_REMOVED.name())
                    .setTimestamp(now);
            return setupBuilder(builder, sfuRtpStreamPodDTO);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }

    private SfuEventReport.Builder setupBuilder(SfuEventReport.Builder builder, SfuRtpStreamPodDTO sfuRtpStreamPodDTO) {
        try {
            String sinkId = sfuRtpStreamPodDTO.getSinkId();
            String sourceId = sfuRtpStreamPodDTO.getSourceId();
            return builder
                    .setMediaUnitId(sfuRtpStreamPodDTO.mediaUnitId)
                    .setSfuId(sfuRtpStreamPodDTO.sfuId.toString())
                    .setSfuName(sfuRtpStreamPodDTO.sfuName)
                    .setTransportId(sfuRtpStreamPodDTO.sfuTransportId.toString())
                    .setStreamId(sfuRtpStreamPodDTO.sfuStreamId.toString())
                    .setSourceId(sourceId)
                    .setSinkId(sinkId)
                    ;
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }
}
