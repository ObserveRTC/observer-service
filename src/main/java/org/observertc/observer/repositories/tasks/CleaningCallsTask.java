package org.observertc.observer.repositories.tasks;

import com.hazelcast.core.DistributedObjectUtil;
import com.hazelcast.map.IMap;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * This is an expensive operation should only be called if there is a suspected not self cleaned repository
 */
@Prototype
public class CleaningCallsTask extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(CleaningCallsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    /**
     * Hazelcast by default uses system.currentTimeMillis unless configuration overrides it.
     */
    private final Long NOW = System.currentTimeMillis();
    private long expiredEntryThresholdInMs = 3600 * 1000;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
                .addActionStage("Clean Maps",
                        // action
                        () -> {
                        /** not updated by clients, only created and removed */
//                            this.cleanMap(this.hazelcastMaps.getCalls());
//                            this.cleanMap(this.hazelcastMaps.getServiceRoomToCallIds());

                            this.cleanMap(this.hazelcastMaps.getClients());
                            this.cleanMap(this.hazelcastMaps.getPeerConnections());
                            this.cleanMap(this.hazelcastMaps.getMediaTracks());
                            this.cleanMap(this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds());
                            this.cleanMap(this.hazelcastMaps.getSFUs());
                            this.cleanMap(this.hazelcastMaps.getSFUTransports());
                            this.cleanMap(this.hazelcastMaps.getSfuInternalInboundRtpPadIdToOutboundRtpPadId());
                            this.cleanMap(this.hazelcastMaps.getSfuStreams());
                            this.cleanMap(this.hazelcastMaps.getSfuSinks());
                            this.cleanMap(this.hazelcastMaps.getGeneralEntries());
                            this.cleanMap(this.hazelcastMaps.getWeakLocks());
                            this.cleanMap(this.hazelcastMaps.getSyncTaskStates());
                            this.cleanMap(this.hazelcastMaps.getRequests());
                            this.cleanMap(this.hazelcastMaps.getEtcMap());
                        })
                .addActionStage("Clean MultiMaps",
                        // action
                        () -> {

                        })

                .build();
    }

    public CleaningCallsTask withExpirationThresholdInMs(long value) {
        if (value < 1) {
            logger.warn("Expiration threshold cannot be less than 1");
            return this;
        }
        this.expiredEntryThresholdInMs = value;
        return this;
    }

    private<K, V> void cleanMap(IMap<K, V> map) {
        var mapName = DistributedObjectUtil.getName(map);
        var localKeySet = map.localKeySet();
        if (localKeySet == null) {
            logger.warn("Null local entryset in {}", mapName);
            return;
        }
        var totalSize = map.size();
        var manuallyDeleted = 0;
        var noAccessedTimestamps = 0;
        for (var entryId : localKeySet) {
            var entryView = map.getEntryView(entryId);
            if (entryView == null) {
                logger.warn("No entryView in map {} for {}", mapName, entryId);
                continue;
            }
            var lastAccessTime = entryView.getLastAccessTime();
            if (lastAccessTime < 0) {
                ++noAccessedTimestamps;
                continue;
            }
            var elapsedInMs = NOW - lastAccessTime;
            if (elapsedInMs < this.expiredEntryThresholdInMs) {
                continue;
            }
            map.delete(entryId);
            ++manuallyDeleted;
        }

        if (0 < noAccessedTimestamps) {
            double percentage = ((double) noAccessedTimestamps / (double) totalSize) * 100.;
            logger.warn("AccessTime was not available for {}%", percentage);
        }
        if (0 < manuallyDeleted) {
            logger.warn("There were {} entries has to be manually deleted from map {} with a threshold of {}. Possible memory leak?", manuallyDeleted, mapName, this.expiredEntryThresholdInMs);
        }
    }
}
