package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.CallDTO;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class FindParentalDTOsTask extends ChainedTask<FindParentalDTOsTask.Report> {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

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
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
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
        callIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.callIds::add);
        return this;
    }

    public FindParentalDTOsTask whereClientIds(Set<UUID> clientIds) {
        Objects.requireNonNull(clientIds);
        clientIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.clientIds::add);
        return this;
    }

    public FindParentalDTOsTask wherePeerConnectionIds(Set<UUID> peerConnectionIds) {
        Objects.requireNonNull(peerConnectionIds);
        peerConnectionIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.peerConnectionIds::add);
        return this;
    }

    public FindParentalDTOsTask whereTrackIds(Set<UUID> trackIds) {
        Objects.requireNonNull(trackIds);
        trackIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.trackIds::add);
        return this;
    }

    @Override
    protected void validate() {

    }
}
