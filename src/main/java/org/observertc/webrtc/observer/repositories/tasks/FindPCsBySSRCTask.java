package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FindPCsBySSRCTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchCallsTask fetchCallsTask;

    private Map<UUID, Set<Long>> serviceSSRCs = new HashMap<>();


    @PostConstruct
    void setup() {

        new Builder<>(this)
            .<Map<UUID, Set<Long>>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> { // input was invoked, so we may got some names through that
                    Iterator<Map.Entry<UUID, Set<Long>>> it = input.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<UUID, Set<Long>> entry = it.next();
                        UUID serviceUUID = entry.getKey();
                        Set<Long> receivedSSRCs = entry.getValue();
                        Set<Long> storedSSRCs = this.serviceSSRCs.get(serviceUUID);
                        if (Objects.isNull(storedSSRCs)) {
                            this.serviceSSRCs.put(serviceUUID, receivedSSRCs);
                        } else {
                            storedSSRCs.addAll(receivedSSRCs);
                        }
                    }
            })
            .<Set<UUID>>addSupplierStage("Fetch PC UUIDs",
            // action
            () -> {
                Set<UUID> result = new HashSet<>();
                this.serviceSSRCs.forEach((serviceUUID, SSRCs) -> {
                    SSRCs.forEach(SSRC -> {
                        Collection<UUID> pcUUIDs = hazelcastMaps.getSSRCsToPCMap(serviceUUID).get(SSRC);
                        if (Objects.isNull(pcUUIDs) || pcUUIDs.size() < 1) {
                            return;
                        }
                        result.addAll(pcUUIDs);
                    });
                });
                return result;
            })
            .addSupplierChainedTask("Fetch PCs", fetchCallsTask)
            .addTerminalPassingStage("Completed")
        .build();
    }

    public FindPCsBySSRCTask whereServiceAndSSRC(UUID serviceUUID, Long SSRC) {
        if (Objects.isNull(SSRC)) {
            return this;
        }
        return this.whereServiceAndSSRC(serviceUUID, Set.of(SSRC));
    }

    public FindPCsBySSRCTask whereServiceAndSSRC(UUID serviceUUID, Collection<Long> SSRCs) {
        if (Objects.isNull(SSRCs)) {
            return this;
        }
        Set<Long> target = this.serviceSSRCs.get(serviceUUID);
        if (Objects.isNull(SSRCs)) {
            target = new HashSet<>();
            this.serviceSSRCs.put(serviceUUID, target);
        }
        target.addAll(SSRCs);
        return this;
    }

    @Override
    protected void validate() {

    }

    private class Carrier {
        Map<UUID, CallDTO> callUUIDTOCallDTOs = new HashMap<>();
        Map<UUID, CallNameEntry> callUUIDToNames = new HashMap<>();
    }

    private class CallNameEntry {
        final UUID serviceUUID;
        final String name;

        private CallNameEntry(UUID serviceUUID, String name) {
            this.serviceUUID = serviceUUID;
            this.name = name;
        }
    }
}
