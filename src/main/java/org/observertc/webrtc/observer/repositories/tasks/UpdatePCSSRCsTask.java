package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class UpdatePCSSRCsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePCSSRCsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPCsTask fetchPCsTask;

    private Map<UUID, Item> items = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
                .addActionStage("Update SSRCs",
                // action
                () -> {
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

    public UpdatePCSSRCsTask withPeerConnectionSSRCs(UUID serviceUUID, UUID pcUUID, Set<Long> SSRCs) {
        this.getItem(serviceUUID, pcUUID).SSRCs.addAll(SSRCs);
        return this;
    }

    private Item getItem(UUID serviceUUID, UUID pcUUID) {
        Item result = this.items.get(pcUUID);
        if (Objects.isNull(result)) {
            result = new Item();
            result.serviceUUID = serviceUUID;
            result.SSRCs = new HashSet<>();
            result.pcUUID = pcUUID;
            this.items.put(pcUUID, result);
        }
        return result;
    }

    private class Item {
        UUID serviceUUID;
        UUID pcUUID;
        Set<Long> SSRCs;
    }
}
