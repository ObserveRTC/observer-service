package org.observertc.webrtc.observer.repositories;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class HazelcastMaps {

    @Inject
    ObserverHazelcast observerHazelcast;

    private IMap<UUID, CallDTO> callDTOs;

    private IMap<UUID, PeerConnectionDTO> pcDTOs;
    private MultiMap<UUID, UUID> callToPCUUIDs;


    @PostConstruct
    void setup() {
        this.callDTOs = observerHazelcast.getInstance().getMap("observertc-calldtos");
        this.pcDTOs = observerHazelcast.getInstance().getMap("observertc-pcdtos");
        this.callToPCUUIDs = observerHazelcast.getInstance().getMultiMap("observertc-call-to-pcuuids");
    }

    public MultiMap<String, UUID> getCallNames(UUID serviceUUID) {
        String mapName = String.format("observertc-callnames-%s", serviceUUID.toString());
        MultiMap<String, UUID> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public MultiMap<UUID, Long> getPCsToSSRCMap(UUID serviceUUID) {
        String mapName = String.format("observertc-pcs-to-ssrcs-%s", serviceUUID.toString());
        MultiMap<UUID, Long> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public MultiMap<Long, UUID> getSSRCsToPCMap(UUID serviceUUID) {
        String mapName = String.format("observertc-ssrc-to-pc-%s", serviceUUID.toString());
        MultiMap<Long, UUID> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public IMap<UUID, CallDTO> getCallDTOs(){
        return this.callDTOs;
    }

    public IMap<UUID, PeerConnectionDTO> getPcDTOs(){
        return this.pcDTOs;
    }

    public MultiMap<UUID, UUID> getCallToPCUUIDs() {
        return this.callToPCUUIDs;
    }
}
