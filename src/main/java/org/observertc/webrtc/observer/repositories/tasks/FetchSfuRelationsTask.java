package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FetchSfuRelationsTask extends ChainedTask<FetchSfuRelationsTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(FetchSfuRelationsTask.class);

    public static final Report EMPTY_REPORT = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    public static class SfuStreamRelations {
        public final UUID trackId;
        public final UUID callId;
        public final UUID clientId;

        public SfuStreamRelations(UUID trackId, UUID callId, UUID clientId) {
            this.trackId = trackId;
            this.callId = callId;
            this.clientId = clientId;
        }
    }

    public static class Report {
        public final Map<UUID, SfuStreamRelations> sfuStreamRelations = new HashMap<>();
        public final Map<UUID, UUID> transportToCallIds = new HashMap<>();
    }

    private Report result = new Report();

    private Set<UUID> sfuStreamIds = new HashSet<>();
    private Set<UUID> sfuTransportIds = new HashSet<>();

    @PostConstruct
    void setup() {

        new Builder<>(this)
                .addActionStage("Collect Sfu Stream relations", () -> {
                    if (this.sfuStreamIds.size() < 1) {
                        return;
                    }
                    this.hazelcastMaps
                            .getSFURtpPods()
                            .getAll(this.sfuStreamIds)
                            .forEach((sfuStreamId, sfuStreamDTO) -> {
                                this.result.sfuStreamRelations.put(sfuStreamId, new SfuStreamRelations(
                                        sfuStreamDTO.trackId,
                                        sfuStreamDTO.callId,
                                        sfuStreamDTO.clientId
                                ));
                            });
                })
                .addActionStage("Collect Sfu Transport relations",
                () -> {
                    if (sfuTransportIds.size() < 1) {
                        return;
                    }
                    this.hazelcastMaps
                            .getSFUTransports()
                            .getAll(this.sfuTransportIds)
                            .forEach((sfuTransportId, sfuTransportDTO) -> {
                                this.result.transportToCallIds.put(sfuTransportId, sfuTransportDTO.callId);
                            });
                })
                .addTerminalSupplier("Completed", () -> {
                    return this.result;
                })
        .build();
    }

    public FetchSfuRelationsTask whereSfuStreamIds(Set<UUID> sfuStreamIds) {
        this.sfuStreamIds.addAll(sfuStreamIds);
        return this;
    }

    public FetchSfuRelationsTask whereSfuTransportIds(Set<UUID> sfuTransportIds) {
        this.sfuTransportIds.addAll(sfuTransportIds);
        return this;
    }


    @Override
    protected void validate() {

    }
}
