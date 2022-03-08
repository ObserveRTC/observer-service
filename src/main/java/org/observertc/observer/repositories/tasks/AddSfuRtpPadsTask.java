package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.dto.*;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.repositories.StoredRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Prototype
public class AddSfuRtpPadsTask extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddSfuRtpPadsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    StoredRequests storedRequests;

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
                .addActionStage("Bind RtpPads to rtp streams and sinks",
                        // action
                        () -> {
                            var sinkIds = this.sfuRtpPadDTOs.values().stream().map(dto -> dto.sinkId).collect(Collectors.toSet());
                            var streamIds = this.sfuRtpPadDTOs.values().stream().map(dto -> dto.streamId).collect(Collectors.toSet());
                            var existingSfuStreams = this.hazelcastMaps.getSfuStreams().getAll(streamIds);
                            var existingSfuSinks = this.hazelcastMaps.getSfuSinks().getAll(sinkIds);
                            Map<UUID, SfuStreamDTO> sfuStreams = new HashMap<>();
                            Map<UUID, SfuSinkDTO> sfuSInks = new HashMap<>();
                            this.sfuRtpPadDTOs.forEach((padId, sfuRtpPadDTO) -> {
                                if (StreamDirection.OUTBOUND.equals(sfuRtpPadDTO.streamDirection) && Objects.nonNull(sfuRtpPadDTO.sinkId))
                                {
                                    var sfuSinkBuilder = SfuSinkDTO.builder();
                                    SfuSinkDTO existingSfuSink = existingSfuSinks.get(sfuRtpPadDTO.sinkId);
                                    if (Objects.nonNull(existingSfuSink)) {
                                        sfuSinkBuilder.from(existingSfuSink);
                                    }
                                    var sfuSink = sfuSinkBuilder
                                            .withSfuId(sfuRtpPadDTO.sfuId)
                                            .withSfuTransportId(sfuRtpPadDTO.transportId)
                                            .withStreamId(sfuRtpPadDTO.streamId)
                                            .withSinkId(sfuRtpPadDTO.sinkId)
                                            .build();
                                    sfuSInks.put(sfuRtpPadDTO.streamId, sfuSink);
                                    this.hazelcastMaps.getSfuSinkIdToRtpPadIds().put(sfuRtpPadDTO.sinkId, sfuRtpPadDTO.sfuPadId);
                                }
                                else if (StreamDirection.INBOUND.equals(sfuRtpPadDTO.streamDirection) && Objects.nonNull(sfuRtpPadDTO.streamId))
                                {
                                    var sfuStreamBuilder = SfuStreamDTO.builder();
                                    SfuStreamDTO existingSfuStream = existingSfuStreams.get(sfuRtpPadDTO.streamId);
                                    if (Objects.nonNull(existingSfuStream)) {
                                        sfuStreamBuilder.from(existingSfuStream);
                                    }
                                    var sfuStream = sfuStreamBuilder
                                            .withSfuId(sfuRtpPadDTO.sfuId)
                                            .withSfuTransportId(sfuRtpPadDTO.transportId)
                                            .withStreamId(sfuRtpPadDTO.streamId)
                                            .build();
                                    sfuStreams.put(sfuRtpPadDTO.streamId, sfuStream);
                                    this.hazelcastMaps.getSfuStreamIdToRtpPadIds().put(sfuRtpPadDTO.streamId, sfuRtpPadDTO.sfuPadId);
                                }
                            });
                            if (0 < sfuStreams.size()) {
                                this.hazelcastMaps.getSfuStreams().putAll(sfuStreams);
                            }
                            if (0 < sfuSInks.size()) {
                                this.hazelcastMaps.getSfuSinks().putAll(sfuSInks);
                            }
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            // sfu bindings are self destructing
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
