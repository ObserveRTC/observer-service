package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuRtpStreamDTO;
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

    private Set<UUID> streamIds = new HashSet<>();
    private Map<UUID, SfuRtpStreamDTO> removedRtpStreams = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<List<SfuEventReport.Builder>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Fetch SfuTransport Ids",
                        () -> this.streamIds,
                        receivedIds -> {
                            this.streamIds.addAll(receivedIds);
                            return this.streamIds;
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
                .<Set<UUID>, Map<UUID, SfuRtpStreamDTO>> addFunctionalStage("Remove Sfu Rtp Stream DTOs",
                        // action
                        identifiers -> {
                            identifiers.forEach(id -> {
                                if (this.removedRtpStreams.containsKey(id)) {
                                    return;
                                }
                                SfuRtpStreamDTO DTO = this.hazelcastMaps.getSFURtpStreams().remove(id);
                                this.removedRtpStreams.put(DTO.transportId, DTO);
                            });
                            return this.removedRtpStreams;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getSFURtpStreams().putAll(this.removedRtpStreams);
                        })
                .<List<SfuEventReport.Builder>> addTerminalFunction("Completed", sfuEventBuildersObj -> {
                    var sfuEventBuilders = (List<SfuEventReport.Builder>) sfuEventBuildersObj;
                    List<SfuEventReport.Builder> result = new LinkedList<>();
                    this.removedRtpStreams.values().stream()
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

    public RemoveSfuRtpStreamsTask whereSfuRtpStreamIds(Set<UUID> streamIds) {
        if (Objects.isNull(streamIds) || streamIds.size() < 1) {
            return this;
        }
        this.streamIds.addAll(streamIds);
        return this;
    }

    public RemoveSfuRtpStreamsTask addRemovedSfuRtpStreamDTO(SfuRtpStreamDTO DTO) {
        if (Objects.isNull(DTO)) {
            return this;
        }
        this.streamIds.add(DTO.streamId);
        this.removedRtpStreams.put(DTO.streamId, DTO);
        return this;
    }

    private SfuEventReport.Builder makeReportBuilder(SfuRtpStreamDTO sfuRtpStreamDTO) {
        Long now = Instant.now().toEpochMilli();
        try {
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_STREAM_REMOVED.name())
                    .setTimestamp(now);
            return setupBuilder(builder, sfuRtpStreamDTO);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }

    private SfuEventReport.Builder setupBuilder(SfuEventReport.Builder builder, SfuRtpStreamDTO sfuRtpStreamDTO) {
        try {
            return builder
                    .setMediaUnitId(sfuRtpStreamDTO.mediaUnitId)
                    .setSfuId(sfuRtpStreamDTO.sfuId.toString())
                    .setTransportId(sfuRtpStreamDTO.transportId.toString())
                    .setRtpStreamId(sfuRtpStreamDTO.streamId.toString())
                    ;
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for SFU DTO", ex);
            return null;
        }
    }
}
