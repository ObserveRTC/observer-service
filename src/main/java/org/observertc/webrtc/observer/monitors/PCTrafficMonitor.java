package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.models.PCTrafficType;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
@Requires(notEnv = Environment.TEST)
public class PCTrafficMonitor extends ExposedMonitorAbstract {
    private static final Logger logger = LoggerFactory.getLogger(PCTrafficMonitor.class);

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    TasksProvider tasksProvider;

    @Inject
    RepositoryProvider repositoryProvider;

    public PCTrafficMonitor() {

    }

    @PostConstruct
    void setup() {
        Config config = new Config();
        config.detailedLogs = true;
        config.maxConsecutiveErrors = 3;
        config.periodTimeInS = 30;
        config.initialDelayInS = 60;
        config.enabled = true;
        config.name = "PCTrafficMonitor";
        this.configure(config);
    }

    @Override
    protected void execute() {
        PeerConnectionsRepository repository = this.repositoryProvider.getPeerConnectionsRepository();
        Map<UUID, PeerConnectionEntity> entities = repository.getLocalEntries();
        logger.info("Locally stored PC keys {}", ObjectToString.toString(entities.keySet()));
        Map<PCTrafficType, Integer> pcNums = new HashMap<>();
        Map<PCTrafficType, Integer> streamNums = new HashMap<>();
        Set<PCTrafficType> trafficTypes = new HashSet<>();

        for (PeerConnectionEntity pcEntity : entities.values()) {
            PCTrafficType trafficType = pcEntity.trafficType;
            Integer streams = 0;
            if (Objects.nonNull(pcEntity.SSRCs)) {
                streams = pcEntity.SSRCs.size();
            }
            pcNums.put(trafficType, pcNums.getOrDefault(trafficType, 0) + 1);
            streamNums.put(trafficType, streamNums.getOrDefault(trafficType, 0) + streams);
            trafficTypes.add(trafficType);
        }

        for (PCTrafficType trafficType : trafficTypes) {
            Integer peerConnections = pcNums.get(trafficType);
            Integer SSRCNums = streamNums.get(trafficType);
            List<Tag> tags = List.of(Tag.of("type", trafficType.name()));
            this.meterRegistry.gauge("observertc_traffic_pcs", tags, peerConnections);
            this.meterRegistry.gauge("observertc_traffic_streams", tags, SSRCNums);
        }
    }
}
