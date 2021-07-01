package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class RefreshTask extends ChainedTask<RefreshTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTask.class);

    public static class Report {
        public Set<UUID> foundClientIds = new HashSet<>();
        public Map<UUID, UUID> foundPeerConnectionIdsToClientIds = new HashMap<>();
        public Map<UUID, UUID> foundMediaTrackIdsToPeerConnectionIds = new HashMap<>();
    }


    private Set<UUID> clientIds = new HashSet<>();
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Set<UUID> mediaTrackIds = new HashSet<>();
    private final Report report = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
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
                            Map<UUID, PeerConnectionDTO> peerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(this.peerConnectionIds);
                            peerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
                                this.report.foundPeerConnectionIdsToClientIds.put(peerConnectionId, peerConnectionDTO.clientId);
                            });
                        })
                .addActionStage("Check Media Tracks",
                        // action
                        () -> {
                            if (this.mediaTrackIds.size() < 1) {
                                return;
                            }
                            Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(this.mediaTrackIds);
                            mediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                this.report.foundMediaTrackIdsToPeerConnectionIds.put(trackId, mediaTrackDTO.peerConnectionId);
                            });
                        })
                .<Report> addTerminalSupplier("Provide the composed report", () -> {
                    return this.report;
                })
                .build();
    }



    public RefreshTask withClientIds(UUID... clientIds) {
        if (Objects.isNull(clientIds)) {
            return this;
        }
        var clientIdsArray = Arrays.asList(clientIds);
        this.clientIds.addAll(clientIdsArray);
        return this;
    }

    public RefreshTask withClientIds(Set<UUID> clientIds) {
        if (Objects.isNull(clientIds)) {
            return this;
        }
        this.clientIds.addAll(clientIds);
        return this;
    }

    public RefreshTask withPeerConnectionIds(UUID... peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds)) {
            return this;
        }
        var peerConnectionIdsArray = Arrays.asList(peerConnectionIds);
        this.peerConnectionIds.addAll(peerConnectionIdsArray);
        return this;
    }

    public RefreshTask withPeerConnectionIds(Set<UUID> peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds)) {
            return this;
        }
        this.peerConnectionIds.addAll(peerConnectionIds);
        return this;
    }

    public RefreshTask withMediaTrackIds(UUID... mediaTrackIds) {
        if (Objects.isNull(mediaTrackIds)) {
            return this;
        }
        var mediaTrackIdsArray = Arrays.asList(mediaTrackIds);
        this.mediaTrackIds.addAll(mediaTrackIdsArray);
        return this;
    }

    public RefreshTask withMediaTrackIds(Set<UUID> mediaTrackIds) {
        if (Objects.isNull(mediaTrackIds)) {
            return this;
        }
        this.mediaTrackIds.addAll(mediaTrackIds);
        return this;
    }

}
