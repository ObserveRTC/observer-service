package org.observertc.observer.micrometer;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectUtil;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class RepositoryMetrics {
    private static final String OBSERVERTC_PREFIX = "observertc";
    private static final String REPOSITORY_PREFIX = "repository";

    private static final String CALLS = String.join("_", OBSERVERTC_PREFIX, REPOSITORY_PREFIX, "calls");
    private static final String CLIENTS = String.join("_", OBSERVERTC_PREFIX, REPOSITORY_PREFIX, "clients");

    private static final String PEER_CONNECTIONS = String.join("_", OBSERVERTC_PREFIX, REPOSITORY_PREFIX, "peer_connections");
    private static final String MEDIA_TRACKS = String.join("_", OBSERVERTC_PREFIX, REPOSITORY_PREFIX, "media_tracks");

    private static final String SFUS = String.join("_", OBSERVERTC_PREFIX, REPOSITORY_PREFIX, "sfus");
    private static final String SFU_TRANSPORTS = String.join("_", OBSERVERTC_PREFIX, REPOSITORY_PREFIX, "sfu_transports");
    private static final String SFU_RTP_PADS = String.join("_", OBSERVERTC_PREFIX, REPOSITORY_PREFIX, "sfu_rtp_pads");

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ObserverConfig.RepositoryConfig config;

    private List<IMap> maps;
    private List<MultiMap> multimaps;
    private Disposable timer = null;

    @PostConstruct
    void init() {
        if (0 < this.config.exposeMetricsPeriodInMins) {
            var worker = Schedulers.computation().createWorker();
            this.timer = worker.schedulePeriodically(this::expose, this.config.exposeMetricsPeriodInMins, this.config.exposeMetricsPeriodInMins, TimeUnit.MINUTES);
        }
        this.maps = List.of(
                this.hazelcastMaps.getCalls(),
                this.hazelcastMaps.getServiceRoomToCallIds(),
                this.hazelcastMaps.getClients(),
                this.hazelcastMaps.getPeerConnections(),
                this.hazelcastMaps.getMediaTracks(),
                this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds(),
                this.hazelcastMaps.getSFUs(),
                this.hazelcastMaps.getSFUTransports(),
                this.hazelcastMaps.getSfuInternalInboundRtpPadIdToOutboundRtpPadId(),
                this.hazelcastMaps.getSfuStreams(),
                this.hazelcastMaps.getSfuSinks(),
                this.hazelcastMaps.getGeneralEntries(),
                this.hazelcastMaps.getWeakLocks(),
                this.hazelcastMaps.getSyncTaskStates(),
                this.hazelcastMaps.getRequests(),
                this.hazelcastMaps.getEtcMap()
        );
        this.multimaps = List.of(
                this.hazelcastMaps.getCallToClientIds(),
                this.hazelcastMaps.getClientToPeerConnectionIds(),
                this.hazelcastMaps.getPeerConnectionToInboundTrackIds(),
                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds(),
                this.hazelcastMaps.getSfuStreamIdToRtpPadIds(),
                this.hazelcastMaps.getSfuSinkIdToRtpPadIds(),
                this.hazelcastMaps.getSfuStreamIdToInternalOutboundRtpPadIds()
        );
    }

    @PreDestroy
    void teardown() {
        if (this.timer != null && !this.timer.isDisposed()) {
            this.timer.dispose();
        }
    }

    private void expose() {
        this.maps.forEach(map -> this.report(map, map.size()));
        this.multimaps.forEach(multimap -> this.report(multimap, multimap.size()));
    }

    private void report(DistributedObject distributedObject, Integer size) {
        var mapName = DistributedObjectUtil.getName(distributedObject);
        var metricName = String.format("repository-%s", mapName);
        this.meterRegistry.gauge(metricName, size);
    }
}
