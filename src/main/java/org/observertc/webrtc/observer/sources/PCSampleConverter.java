package org.observertc.webrtc.observer.sources;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.common.MinuteToTimeZoneId;
import org.observertc.webrtc.observer.common.TimeLimitedMap;
import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.observer.dto.pcsamples.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.entities.ServiceMapEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.repositories.ServiceMapsRepository;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.observer.samples.SourceSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Prototype
class PCSampleConverter implements Function<SourceSample, ObservedPCS> {

    private static final Logger logger = LoggerFactory.getLogger(PCSampleConverter.class);

    @Inject
    MinuteToTimeZoneId minuteToTimeZoneId;

    @Inject
    ServiceMapsRepository serviceMapsRepository;

    private final TimeLimitedMap<UUID, String> peerConnectionServices;
    private final FlawMonitor flawMonitor;

    private final ObserverConfig.SourcesConfig.PCSamplesConfig config;

    public PCSampleConverter(
            ObserverConfig observerConfig,
            MonitorProvider monitorProvider)
    {
        this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
        Duration threshold = Duration.ofSeconds(observerConfig.evaluators.peerConnectionMaxIdleTimeInS);
        this.peerConnectionServices = new TimeLimitedMap<>(threshold);
        this.config = observerConfig.sources.pcSamples;
    }

    @Override
    public ObservedPCS apply(SourceSample sourceSample) throws Throwable {
        if (Utils.anyNull(sourceSample, sourceSample.sample)) {
            return null;
        }
        if (!this.config.enabled) {
            return null;
        }
        try {
            return this.convert(sourceSample);
        } catch (Throwable t) {
            this.flawMonitor.makeLogEntry()
                    .withLogger(logger)
                    .withMessage("Exception occurred during conversion")
                    .withException(t)
                    .complete();
        }
        return null;
    }

    private ObservedPCS convert(SourceSample sourceSample) throws Throwable {
        PeerConnectionSample sample = (PeerConnectionSample) sourceSample.sample;
        String serviceName = this.getServiceName(sourceSample.serviceUUID, sourceSample.peerConnectionUUID);
        if (Objects.isNull(serviceName)) {
            if (this.config.dropUnknownServiceName) {
                return null;
            }
            serviceName = this.config.defaultServiceName;
        }
        String timeZoneId = null;
        if (Objects.nonNull(sample.timeZoneOffsetInMinute)) {
            ZoneOffset zoneOffset = minuteToTimeZoneId.apply(sample.timeZoneOffsetInMinute.intValue());
            if (Objects.nonNull(zoneOffset)) {
                timeZoneId = zoneOffset.getId();
            }
        }
        String marker = sample.marker;
        return ObservedPCS.of(
                sourceSample.serviceUUID,
                sourceSample.mediaUnitId,
                sourceSample.peerConnectionUUID,
                sample,
                timeZoneId,
                serviceName,
                marker,
                sample.timestamp
        );
    }

    private String getServiceName(UUID serviceUUID, UUID pcUUID) {
        if (Objects.isNull(pcUUID)) {
            Optional<ServiceMapEntity> entityHolder = this.serviceMapsRepository.findByUUID(serviceUUID);
            if (!entityHolder.isPresent()) {
                return null;
            }
            return entityHolder.get().name;
        }
        boolean pcExists = this.peerConnectionServices.containsKey(pcUUID);
        if (!pcExists) {
            Optional<ServiceMapEntity> entityHolder = this.serviceMapsRepository.findByUUID(serviceUUID);
            if (entityHolder.isPresent()) {
                ServiceMapEntity entity = entityHolder.get();
                this.peerConnectionServices.put(pcUUID, entity.name);
            } else {
                this.peerConnectionServices.put(pcUUID, null);
            }
        }
        return this.peerConnectionServices.get(pcUUID);
    }
}
