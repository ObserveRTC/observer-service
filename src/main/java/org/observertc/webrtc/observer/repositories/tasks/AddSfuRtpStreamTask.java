package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuRtpStreamDTO;
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
public class AddSfuRtpStreamTask extends ChainedTask<List<SfuEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(AddSfuRtpStreamTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Map<UUID, SfuRtpStreamDTO> sfuRtpStreamDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
                .<Map<UUID, SfuRtpStreamDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedSfuRtpStreamDTOs -> {
                            if (Objects.nonNull(receivedSfuRtpStreamDTOs)) {
                                this.sfuRtpStreamDTOs.putAll(receivedSfuRtpStreamDTOs);
                            }
                        }
                )
                .<Map<UUID, SfuTransportDTO>> addBreakCondition((resultHolder) -> {
                    if (this.sfuRtpStreamDTOs.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add Sfu Rtp Stream DTOs",
                // action
                () -> {
                    hazelcastMaps.getSFURtpStreams().putAll(this.sfuRtpStreamDTOs);
                },
                // rollback
                (inputHolder, thrownException) -> {
                    for (UUID rtpStreamId : this.sfuRtpStreamDTOs.keySet()) {
                        this.hazelcastMaps.getSFURtpStreams().remove(rtpStreamId);
                    }
                })
                .addTerminalSupplier("Completed", () -> {
                    List<SfuEventReport.Builder> result = this.sfuRtpStreamDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
    }

    public AddSfuRtpStreamTask withSfuRtpStreamDTO(SfuRtpStreamDTO sfuRtpStreamDTO) {
        if (Objects.isNull(sfuRtpStreamDTO)) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        this.sfuRtpStreamDTOs.put(sfuRtpStreamDTO.streamId, sfuRtpStreamDTO);
        return this;
    }

    public AddSfuRtpStreamTask withSfuRtpStreamDTOs(SfuRtpStreamDTO... sfuRtpStreamDTOs) {
        if (Objects.isNull(sfuRtpStreamDTOs) || sfuRtpStreamDTOs.length < 1) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        Arrays.stream(sfuRtpStreamDTOs).forEach(sfuRtpStreamDTO -> {
            this.sfuRtpStreamDTOs.put(sfuRtpStreamDTO.streamId, sfuRtpStreamDTO);
        });
        return this;
    }

    public AddSfuRtpStreamTask withSfuRtpStreamDTOs(Map<UUID, SfuRtpStreamDTO> sfuRtpStreamDTOs) {
        if (Objects.isNull(sfuRtpStreamDTOs) || sfuRtpStreamDTOs.size() < 1) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        this.sfuRtpStreamDTOs.putAll(sfuRtpStreamDTOs);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuRtpStreamDTO sfuRtpStreamDTO) {
        try {
            return SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_STREAM_ADDED.name())
                    .setSfuId(sfuRtpStreamDTO.sfuId.toString())
                    .setTransportId(sfuRtpStreamDTO.transportId.toString())
                    .setRtpStreamId(sfuRtpStreamDTO.streamId.toString())
                    .setMediaUnitId(sfuRtpStreamDTO.mediaUnitId)
                    .setTimestamp(sfuRtpStreamDTO.added);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for Sfu stream DTO", ex);
            return null;
        }
    }
}
