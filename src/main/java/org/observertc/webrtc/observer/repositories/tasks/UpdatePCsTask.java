package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class UpdatePCsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePCsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPCsTask fetchPCsTask;

    private boolean updatePCDTOs = true;
    private boolean updateSSRCs = true;
    private Map<UUID, Item> items = new HashMap<>();
//    private Map<UUID, NewPeerConnectionEntity> peerConnectionEntities = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
                .addActionStage("Update Peer Connection DTOs",
                // action
                () -> {
                    if (!this.updatePCDTOs) {
                        return;
                    }
                    Map<UUID, PeerConnectionDTO> pcDTOs = hazelcastMaps.getPcDTOs().getAll(this.items.keySet());
                    for (Item pcItem : this.items.values()) {
                        if (Objects.isNull(pcItem.pcDTO)) {
                            continue;
                        }
                        PeerConnectionDTO storedPCDTO = pcDTOs.remove(pcItem.pcUUID);
                        if (Objects.isNull(storedPCDTO)) {
                            getLogger().warn("The given Peer Connection is not stored {}", pcItem.pcDTO);
                            continue;
                        }
                        if (!storedPCDTO.equals(pcItem.pcDTO)) {
                            pcDTOs.put(pcItem.pcUUID, pcItem.pcDTO);
                        }
                    }
                    if (0 < pcDTOs.size()) {
                        hazelcastMaps.getPcDTOs().putAll(pcDTOs);
                    }
                })
                .addActionStage("Update SSRCs",
                // action
                () -> {
                    if (!this.updateSSRCs) {
                        return;
                    }
                    for (Item pcItem : this.items.values()) {

                        Collection<Long> SSRCs = hazelcastMaps.getPCsToSSRCMap(pcItem.serviceUUID).get(pcItem.pcUUID);
                        Set<Long> removedSSRCs = SSRCs.stream().filter(ssrc -> !pcItem.SSRCs.contains(ssrc)).collect(Collectors.toSet());
                        if (0 < removedSSRCs.size()) {
                            for (Long removedSSRC : removedSSRCs) {
                                hazelcastMaps.getSSRCsToPCMap(pcItem.serviceUUID).remove(removedSSRC, pcItem.pcUUID);
                                hazelcastMaps.getPCsToSSRCMap(pcItem.serviceUUID).remove(pcItem.pcUUID, removedSSRC);
                            }
                        }
                        Set<Long> addedSSRCs = pcItem.SSRCs.stream().filter(ssrc -> !SSRCs.contains(ssrc)).collect(Collectors.toSet());
                        if (0 < addedSSRCs.size()) {
                            for (Long addedSSRC : addedSSRCs) {
                                hazelcastMaps.getSSRCsToPCMap(pcItem.serviceUUID).put(addedSSRC, pcItem.pcUUID);
                                hazelcastMaps.getPCsToSSRCMap(pcItem.serviceUUID).put(pcItem.pcUUID, addedSSRC);
                            }
                        }
                    }
                })
                .build();
    }

    public UpdatePCsTask withPeerConnectionSSRCs(UUID serviceUUID, UUID pcUUID, Set<Long> SSRCs) {
        this.getItem(serviceUUID, pcUUID).SSRCs.addAll(SSRCs);
        return this;
    }

    public UpdatePCsTask withPCDTO(UUID pcUUID, PeerConnectionDTO pcDTO) {
        this.getItem(pcDTO.serviceUUID, pcUUID).pcDTO = pcDTO;
        return this;
    }

    private Item getItem(UUID serviceUUID, UUID pcUUID) {
        Item result = this.items.get(pcUUID);
        if (Objects.isNull(result)) {
            result = new Item();
            result.pcDTO = null;
            result.serviceUUID = serviceUUID;
            result.SSRCs = new HashSet<>();
            this.items.put(pcUUID, result);
        }
        return result;
    }

    public UpdatePCsTask doNotUpdateSSRCs() {
        this.updateSSRCs = false;
        return this;
    }

    public UpdatePCsTask doNotUpdatePCDTOs() {
        this.updatePCDTOs = false;
        return this;
    }

    private class Item {
        UUID serviceUUID;
        UUID pcUUID;
        PeerConnectionDTO pcDTO;
        Set<Long> SSRCs;
    }
}
