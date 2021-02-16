package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FindCallTask extends ChainedTask<Map<UUID, CallEntity>> {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FindCallsByNameTask findCallsByNameTask;

    @Inject
    FindPCsBySSRCTask findPCsBySSRCTask;

    @Inject
    FetchCallsTask fetchCallsTask;

    private boolean haveCallNames = false;
    private boolean haveSSRCs = false;

    private Map<UUID, CallEntity> foundCallEntities = new HashMap<>();

    @PostConstruct
    void setup() {

        new Builder<>(this)
            .addActionStage("Fetch Calls by name",
            // action
            () -> {
                if (!this.haveCallNames) {
                    return;
                }
                if (!findCallsByNameTask.execute().succeeded()) {
                    return;
                }
                Map<UUID, CallEntity> callEntities = findCallsByNameTask.getResult();
                if (Objects.nonNull(callEntities)) {
                    this.foundCallEntities.putAll(callEntities);
                }
            })
            .addActionStage("Fetch Calls by SSRCs",
            // action
            () -> {
                if (!this.haveSSRCs) {
                    return;
                }
                if (!findPCsBySSRCTask.execute().succeeded()) {
                    return;
                }
                Map<UUID, PeerConnectionEntity> pcEntities = findPCsBySSRCTask.getResult();
                if (Objects.nonNull(pcEntities) && 0 < pcEntities.size()) {
                    Set<UUID> callUUIDs = pcEntities.values().stream().map(pc -> pc.callUUID).collect(Collectors.toSet());
                    if (!fetchCallsTask.whereCallUUIDs(callUUIDs).execute().succeeded()) {
                        return;
                    }
                    Map<UUID, CallEntity> callEntities = fetchCallsTask.getResult();
                    if (Objects.nonNull(callEntities)) {
                        this.foundCallEntities.putAll(callEntities);
                    }
                }
            })
            .<Map<UUID, CallEntity>>addTerminalSupplier("Comleted", () -> {
                return Collections.unmodifiableMap(this.foundCallEntities);
            })
        .build();
    }

    public FindCallTask whereCallName(UUID serviceUUID, String callName) {
        this.findCallsByNameTask.whereCallName(serviceUUID, callName);
        this.haveCallNames = true;
        return this;
    }

    public FindCallTask whereSSRC(UUID serviceUUID, Set<Long> SSRCs) {
        this.findPCsBySSRCTask.whereServiceAndSSRC(serviceUUID, SSRCs);
        this.haveSSRCs = true;
        return this;
    }


    @Override
    protected void validate() {

    }

}
