package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FindParentalDTOsTask extends ChainedTask<FindParentalDTOsTask.Report> {

    @Inject
    HazelcastMaps hazelcastMaps;

    private Set<UUID> trackIds = new HashSet<>();
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Set<UUID> clientIds = new HashSet<>();
    private Set<UUID> callIds = new HashSet<>();

    public static class Report {
        public Map<UUID, CallDTO> callDTOs = new HashMap<>();
        public Map<UUID, ClientDTO> clientDTOs = new HashMap<>();
        public Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();
        public Map<UUID, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();
    }

    private Report result = new Report();

    @PostConstruct
    void setup() {

        new Builder<>(this)
                .addActionStage("Collect Media track DTOs", () -> {
                    if (this.trackIds.size() < 1) {
                        return;
                    }
                    var mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(this.trackIds);
                    mediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                        this.peerConnectionIds.add(mediaTrackDTO.peerConnectionId);
                    });
                    this.result.mediaTrackDTOs.putAll(mediaTrackDTOs);
                })
                .addActionStage("Collect Peer Connection DTOs", () -> {
                    if (this.peerConnectionIds.size() < 1) {
                        return;
                    }
                    var peerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(this.peerConnectionIds);
                    peerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
                        this.clientIds.add(peerConnectionDTO.clientId);
                    });
                    this.result.peerConnectionDTOs.putAll(peerConnectionDTOs);
                })
                .addActionStage("Collect Client DTOs", () -> {
                    if (this.clientIds.size() < 1) {
                        return;
                    }
                    var clientDTOs = this.hazelcastMaps.getClients().getAll(this.clientIds);
                    clientDTOs.forEach((clientId, clientDTO) -> {
                        this.callIds.add(clientDTO.callId);
                    });
                    this.result.clientDTOs.putAll(clientDTOs);
                })
                .addActionStage("Collect Call DTOs", () -> {
                    if (this.callIds.size() < 1) {
                        return;
                    }
                    var callDTOs = this.hazelcastMaps.getCalls().getAll(this.callIds);
                    this.result.callDTOs.putAll(callDTOs);
                })
            .addTerminalSupplier("Fetch Call Ids", () -> {
                return this.result;
            })
        .build();
    }

    public FindParentalDTOsTask whereCallIds(Set<UUID> callIds) {
        Objects.requireNonNull(callIds);
        this.callIds.addAll(callIds);
        return this;
    }

    public FindParentalDTOsTask whereClientIds(Set<UUID> clientIds) {
        Objects.requireNonNull(clientIds);
        this.clientIds.addAll(clientIds);
        return this;
    }

    public FindParentalDTOsTask wherePeerConnectionIds(Set<UUID> peerConnectionIds) {
        Objects.requireNonNull(peerConnectionIds);
        this.peerConnectionIds.addAll(peerConnectionIds);
        return this;
    }

    public FindParentalDTOsTask whereTrackIds(Set<UUID> trackIds) {
        Objects.requireNonNull(trackIds);
        this.trackIds.addAll(trackIds);
        return this;
    }

    @Override
    protected void validate() {

    }
}
