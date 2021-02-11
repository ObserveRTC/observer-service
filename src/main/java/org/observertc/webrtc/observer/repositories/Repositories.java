package org.observertc.webrtc.observer.repositories;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.dto.CallDTO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class Repositories {
    static final String CALLS_REPOSITORY_NAME = "observertc-callrep";
    static final String SERVICE_REPOSITORY_NAME = "observertc-services";

    @Inject
    ObserverHazelcast observerHazelcast;

    private IMap<UUID, CallDTO> callDTOs;

    @PostConstruct
    void setup() {
        this.callDTOs = observerHazelcast.getInstance().getMap("observertc-calldtos");
    }

    public MultiMap<String, UUID> getCallNames(UUID serviceUUID) {
        String mapName = String.format("observertc-callnames-%s", serviceUUID.toString());
        MultiMap<String, UUID> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public IMap<Long, UUID> getSSRCToCallMap(UUID serviceUUID) {
        String mapName = String.format("observertc-ssrc-to-call-%s", serviceUUID.toString());
        IMap<Long, UUID> result = observerHazelcast.getInstance().getMap(mapName);
        return result;
    }

    public MultiMap<UUID, Long> getCallToSSRCMap(UUID serviceUUID) {
        String mapName = String.format("observertc-call-to-ssrcs-%s", serviceUUID.toString());
        MultiMap<UUID, Long> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public IMap<UUID, CallDTO> getCallDTOs(){
        return this.callDTOs;
    }
}
