package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Prototype
public class AddSfuRtpPadsTask extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddSfuRtpPadsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    private Map<UUID, SfuRtpPadDTO> sfuRtpPadDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
                .<Map<UUID, SfuRtpPadDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedSfuRtpStreamPadDTOs -> {
                            if (Objects.nonNull(receivedSfuRtpStreamPadDTOs)) {
                                this.sfuRtpPadDTOs.putAll(receivedSfuRtpStreamPadDTOs);
                            }
                        }
                )
                .<Map<UUID, SfuTransportDTO>> addBreakCondition((resultHolder) -> {
                    if (this.sfuRtpPadDTOs.size() < 1) {
                        resultHolder.set(null);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add SfuRtpPad DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getSFURtpPads().putAll(this.sfuRtpPadDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID rtpSourceId : this.sfuRtpPadDTOs.keySet()) {
                                this.hazelcastMaps.getSFURtpPads().remove(rtpSourceId);
                            }
                        })
                .addActionStage("Bind RTP Pad Ids to SFU Transport",
                        // action
                        () -> {
                            this.sfuRtpPadDTOs.values().stream()
                                    .filter(Objects::nonNull)
                                    .forEach(sfuRtpPadDTO -> {
                                        this.hazelcastMaps.getSfuTransportToSfuRtpPadIds().put(sfuRtpPadDTO.transportId, sfuRtpPadDTO.rtpPadId);
                                    });
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.sfuRtpPadDTOs.values().stream()
                                    .filter(Objects::nonNull)
                                    .forEach(sfuRtpPadDTO -> {
                                        this.hazelcastMaps.getSfuTransportToSfuRtpPadIds().remove(sfuRtpPadDTO.transportId, sfuRtpPadDTO.rtpPadId);
                                    });
                        })
                .addTerminalPassingStage("Completed")
                .build();
        this.withLogger(logger);
    }

    public AddSfuRtpPadsTask withSfuRtpPadDTOs(Map<UUID, SfuRtpPadDTO> sfuRtpPadDTO) {
        if (Objects.isNull(sfuRtpPadDTO) || sfuRtpPadDTO.size() < 1) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        sfuRtpPadDTO.values().stream().filter(Utils::expensiveNonNullCheck).forEach(sfuRtpPadDTOs -> {
            this.sfuRtpPadDTOs.put(sfuRtpPadDTOs.rtpPadId, sfuRtpPadDTOs);
        });
        return this;
    }
}
