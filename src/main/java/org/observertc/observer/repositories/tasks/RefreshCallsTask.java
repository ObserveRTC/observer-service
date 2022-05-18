package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Prototype
public class RefreshCallsTask extends ChainedTask<RefreshCallsTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshCallsTask.class);

    public static class Report {
        public Set<UUID> foundClientIds = new HashSet<>();
        public Set<UUID> foundTrackIds = new HashSet<>();
        public Set<UUID> foundPeerConnectionIds = new HashSet<>();
    }


    private Set<UUID> clientIds = new HashSet<>();
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Set<UUID> mediaTrackIds = new HashSet<>();
    private final Report report = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Report>(this)
                .addActionStage("Check Clients",
                        // action
                        () -> {
                            var nullIds = new HashSet<UUID>();
                            var clientIds = Utils.trash(this.clientIds.stream(), Objects::nonNull, nullIds).collect(Collectors.toSet());
                            if (0 < nullIds.size()) {
                                logger.warn("There were null peer connection ids.");
                                this.clientIds = clientIds;
                            }
                            if (clientIds.size() < 1) {
                                return;
                            }

                            Map<UUID, ClientDTO> clientDTOs = this.hazelcastMaps.getClients().getAll(clientIds);
                            this.report.foundClientIds.addAll(clientDTOs.keySet());
                        })
                .addActionStage("Update client touches", () -> {
                    var now = Instant.now().toEpochMilli();
                    var touches = clientIds.stream()
                            .filter(Objects::nonNull)
                            .collect(
                                    Collectors.toMap(
                                            Function.identity(),
                                            id -> now
                                    )
                            );
                    this.hazelcastMaps.getRefreshedClients().putAll(touches);
                })
                .addActionStage("Check Peer Connections",
                        // action
                        () -> {
                            var nullIds = new HashSet<UUID>();
                            var peerConnectionIds = Utils.trash(this.peerConnectionIds.stream(), Objects::nonNull, nullIds).collect(Collectors.toSet());
                            if (0 < nullIds.size()) {
                                logger.warn("There were null peer connection ids.");
                            }
                            if (peerConnectionIds.size() < 1) {
                                return;
                            }
                            var peerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(peerConnectionIds);
                            this.report.foundPeerConnectionIds.addAll(peerConnectionDTOs.keySet());
                        })
                .addActionStage("Check Media Tracks",
                        // action
                        () -> {
                            var nullIds = new HashSet<UUID>();
                            var mediaTrackIds = Utils.trash(this.mediaTrackIds.stream(), Objects::nonNull, nullIds).collect(Collectors.toSet());
                            if (0 < nullIds.size()) {
                                logger.warn("There were null media track ids.");
                            }
                            if (mediaTrackIds.size() < 1) {
                                return;
                            }
                            Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(mediaTrackIds);
                            this.report.foundTrackIds.addAll(mediaTrackDTOs.keySet());
                        })
                .<Report> addTerminalSupplier("Provide the composed report", () -> {
                    return this.report;
                })
                .build();
    }




    public RefreshCallsTask withClientIds(Set<UUID> clientIds) {
        if (Objects.isNull(clientIds)) {
            return this;
        }
        clientIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.clientIds::add);
        return this;
    }


    public RefreshCallsTask withPeerConnectionIds(Set<UUID> peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds)) {
            return this;
        }
        peerConnectionIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.peerConnectionIds::add);
        return this;
    }


    public RefreshCallsTask withMediaTrackIds(Set<UUID> mediaTrackIds) {
        if (Objects.isNull(mediaTrackIds)) {
            return this;
        }
        mediaTrackIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.mediaTrackIds::add);
        return this;
    }

}
