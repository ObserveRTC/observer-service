package org.observertc.webrtc.observer.evaluators;

import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.models.PCTrafficType;
import org.observertc.webrtc.observer.tasks.PeerConnectionsTrafficUpdater;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Singleton
public class PCTrafficEvaluator implements Consumer<List<PCTrafficState>> {
    private static final Logger logger = LoggerFactory.getLogger(PCTrafficEvaluator.class);

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    TasksProvider tasksProvider;

    private final Map<UUID, TrafficState> trafficStates = new HashMap<>();

    public PCTrafficEvaluator() {

    }

    @PostConstruct
    void setup() {

    }

    @PreDestroy
    void teardown() {

    }

    @Override
    public void accept(List<PCTrafficState> samples) throws Throwable {
        if (samples.size() < 1) {
            return;
        }

        for (PCTrafficState sample : samples) {
            TrafficState trafficState = this.trafficStates.get(sample.peerConnectionUUID);
            if (Objects.isNull(trafficState)) {
                trafficState = new TrafficState();
                this.trafficStates.put(sample.peerConnectionUUID, trafficState);
            }
            trafficState.trafficType = sample.trafficType;
        }

        PeerConnectionsTrafficUpdater task = this.tasksProvider.getPeerConnectionsTrafficUpdater();
        Iterator<Map.Entry<UUID, TrafficState>> it = this.trafficStates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, TrafficState> entry = it.next();
            UUID pcUUID = entry.getKey();
            TrafficState trafficState = entry.getValue();
            task.forTrafficState(pcUUID, trafficState.trafficType);
        }
        if (!task.execute().succeeded()) {
            return;
        }

        Set<UUID> updatedPCs = task.getResult();
        if (Objects.isNull(updatedPCs)) {
            return;
        }

        Instant now = Instant.now();
        // remove updated ones
        it = this.trafficStates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, TrafficState> entry = it.next();
            UUID pcUUID = entry.getKey();
            TrafficState trafficState = entry.getValue();
            if (updatedPCs.contains(pcUUID)) {
                it.remove();
                continue;
            }
            if (150 < Duration.between(trafficState.created, now).getSeconds()) {
                logger.warn("A trafficUpdate never bound to a peer connection", ObjectToString.toString(trafficState));
                it.remove();
            }
        }
    }

    private class TrafficState {
        PCTrafficType trafficType = null;
        final Instant created = Instant.now();
    }

}
