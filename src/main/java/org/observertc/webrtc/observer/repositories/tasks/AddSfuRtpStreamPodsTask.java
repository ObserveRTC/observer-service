package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuRtpStreamPodDTO;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class AddSfuRtpStreamPodsTask extends ChainedTask<List<SfuEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(AddSfuRtpStreamPodsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Map<UUID, SfuRtpStreamPodDTO> sfuRtpStreamPodDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
                .<Map<UUID, SfuRtpStreamPodDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedSfuRtpStreamPodDTOs -> {
                            if (Objects.nonNull(receivedSfuRtpStreamPodDTOs)) {
                                this.sfuRtpStreamPodDTOs.putAll(receivedSfuRtpStreamPodDTOs);
                            }
                        }
                )
                .<Map<UUID, SfuTransportDTO>> addBreakCondition((resultHolder) -> {
                    if (this.sfuRtpStreamPodDTOs.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add Sfu Rtp Stream DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getSFURtpPods().putAll(this.sfuRtpStreamPodDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID rtpSourceId : this.sfuRtpStreamPodDTOs.keySet()) {
                                this.hazelcastMaps.getSFURtpPods().remove(rtpSourceId);
                            }
                        })
                .addActionStage("Add Sfu Stream Ids to Rtp Pods",
                        // action
                        () -> {
                            this.sfuRtpStreamPodDTOs.forEach((rtpPodId, sfuRtpStreamPodDTO) -> {
                                this.hazelcastMaps.getSfuStreamToRtpPodIds().put(sfuRtpStreamPodDTO.sfuStreamId, rtpPodId);
                            });
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.sfuRtpStreamPodDTOs.forEach((rtpPodId, sfuRtpStreamPodDTO) -> {
                                this.hazelcastMaps.getSfuStreamToRtpPodIds().remove(sfuRtpStreamPodDTO.sfuStreamId, rtpPodId);
                            });
                        })
                .addTerminalSupplier("Completed", () -> {
                    List<SfuEventReport.Builder> result = this.sfuRtpStreamPodDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
        this.withLogger(logger);
    }

    public AddSfuRtpStreamPodsTask withSfuRtpStreamDTO(SfuRtpStreamPodDTO sfuRtpStreamPodDTO) {
        if (Objects.isNull(sfuRtpStreamPodDTO)) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        this.sfuRtpStreamPodDTOs.put(sfuRtpStreamPodDTO.sfuPodId, sfuRtpStreamPodDTO);
        return this;
    }

    public AddSfuRtpStreamPodsTask withSfuRtpStreamDTOs(SfuRtpStreamPodDTO... sfuRtpStreamPodDTOs) {
        if (Objects.isNull(sfuRtpStreamPodDTOs) || sfuRtpStreamPodDTOs.length < 1) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        Arrays.stream(sfuRtpStreamPodDTOs).forEach(sfuRtpStreamDTO -> {
            this.sfuRtpStreamPodDTOs.put(sfuRtpStreamDTO.sfuPodId, sfuRtpStreamDTO);
        });
        return this;
    }

    public AddSfuRtpStreamPodsTask withSfuRtpStreamDTOs(Map<UUID, SfuRtpStreamPodDTO> sfuRtpStreamPodDTOs) {
        if (Objects.isNull(sfuRtpStreamPodDTOs) || sfuRtpStreamPodDTOs.size() < 1) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        this.sfuRtpStreamPodDTOs.putAll(sfuRtpStreamPodDTOs);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuRtpStreamPodDTO sfuRtpStreamPodDTO) {
        try {
            String sourceId = sfuRtpStreamPodDTO.getSourceId();
            String sinkId = sfuRtpStreamPodDTO.getSinkId();
            return SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_STREAM_ADDED.name())
                    .setSfuId(sfuRtpStreamPodDTO.sfuId.toString())
                    .setSfuName(sfuRtpStreamPodDTO.sfuName)
                    .setTransportId(sfuRtpStreamPodDTO.sfuTransportId.toString())
                    .setStreamId(sfuRtpStreamPodDTO.sfuStreamId.toString())
                    .setSourceId(sourceId)
                    .setSinkId(sinkId)
                    .setMediaUnitId(sfuRtpStreamPodDTO.mediaUnitId)
                    .setTimestamp(sfuRtpStreamPodDTO.added);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for Sfu stream DTO", ex);
            return null;
        }
    }
}
