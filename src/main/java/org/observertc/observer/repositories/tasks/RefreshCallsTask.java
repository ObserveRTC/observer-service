package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class RefreshCallsTask extends ChainedTask<RefreshCallsTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshCallsTask.class);

    public static class Report {
        public Set<UUID> foundClientIds = new HashSet<>();
        public Set<UUID> foundTrackIds = new HashSet<>();
        public Set<UUID> foundPeerConnectionIds = new HashSet<>();
//        public Map<UUID, UUID> foundPeerConnectionIdsToClientIds = new HashMap<>();
//        public Map<UUID, UUID> foundMediaTrackIdsToPeerConnectionIds = new HashMap<>();
//        public Map<UUID, UUID> foundInboundTrackIdToOutboundTrackIds = new HashMap<>();
    }


    private Set<UUID> clientIds = new HashSet<>();
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Set<UUID> mediaTrackIds = new HashSet<>();
    private final Report report = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Report>(this)
                .addActionStage("Check Clients",
                        // action
                        () -> {
                            if (this.clientIds.size() < 1) {
                                return;
                            }
                            Map<UUID, ClientDTO> clientDTOs = this.hazelcastMaps.getClients().getAll(this.clientIds);
                            this.report.foundClientIds.addAll(clientDTOs.keySet());
                        })
                .addActionStage("Check Peer Connections",
                        // action
                        () -> {
                            if (this.peerConnectionIds.size() < 1) {
                                return;
                            }
                            var peerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(this.peerConnectionIds);
                            this.report.foundPeerConnectionIds.addAll(peerConnectionDTOs.keySet());
                        })
                .addActionStage("Check Media Tracks",
                        // action
                        () -> {
                            if (this.mediaTrackIds.size() < 1) {
                                return;
                            }
                            Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(this.mediaTrackIds);
                            this.report.foundTrackIds.addAll(mediaTrackDTOs.keySet());
                        })
//                .addActionStage("Check Inbound TrackIds to Outbound TrackIds",
//                        // action
//                        () -> {
//                            if (this.inboundTrackIds.size() < 1) {
//                                return;
//                            }
//                            Map<UUID, UUID> inboundTrackIdsToOutboundTrackIds = this.hazelcastMaps.getInboundTrackToOutboundTracks().getAll(this.inboundTrackIds);
//                            this.report.foundInboundTrackIdToOutboundTrackIds.putAll(inboundTrackIdsToOutboundTrackIds);
//                        })
                .<Report> addTerminalSupplier("Provide the composed report", () -> {
                    return this.report;
                })
                .build();
    }



//    public RefreshCallsTask withClientIds(UUID... clientIds) {
//        if (Objects.isNull(clientIds)) {
//            return this;
//        }
//        var clientIdsArray = Arrays.asList(clientIds);
//        this.clientIds.addAll(clientIdsArray);
//        return this;
//    }

    public RefreshCallsTask withClientIds(Set<UUID> clientIds) {
        if (Objects.isNull(clientIds)) {
            return this;
        }
        this.clientIds.addAll(clientIds);
        return this;
    }

//    public RefreshCallsTask withPeerConnectionIds(UUID... peerConnectionIds) {
//        if (Objects.isNull(peerConnectionIds)) {
//            return this;
//        }
//        var peerConnectionIdsArray = Arrays.asList(peerConnectionIds);
//        this.peerConnectionIds.addAll(peerConnectionIdsArray);
//        return this;
//    }

    public RefreshCallsTask withPeerConnectionIds(Set<UUID> peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds)) {
            return this;
        }
        this.peerConnectionIds.addAll(peerConnectionIds);
        return this;
    }

//    public RefreshCallsTask withMediaTrackIds(UUID... mediaTrackIds) {
//        if (Objects.isNull(mediaTrackIds)) {
//            return this;
//        }
//        var mediaTrackIdsArray = Arrays.asList(mediaTrackIds);
//        this.mediaTrackIds.addAll(mediaTrackIdsArray);
//        return this;
//    }

    public RefreshCallsTask withMediaTrackIds(Set<UUID> mediaTrackIds) {
        if (Objects.isNull(mediaTrackIds)) {
            return this;
        }
        this.mediaTrackIds.addAll(mediaTrackIds);
        return this;
    }

}
