package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
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
    ExposedMetrics exposedMetrics;

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
                .addTerminalPassingStage("Completed")
                .build();
        this.withLogger(logger);
    }

    public AddSfuRtpPadsTask withSfuRtpPadDTOs(Map<UUID, SfuRtpPadDTO> sfuRtpPadDTO) {
        if (Objects.isNull(sfuRtpPadDTO) || sfuRtpPadDTO.size() < 1) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        this.sfuRtpPadDTOs.putAll(sfuRtpPadDTO);
        return this;
    }
}
