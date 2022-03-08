package org.observertc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.tasks.*;
import org.observertc.observer.samples.*;
import org.observertc.webrtc.observer.samples.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class AddNewSfuEntities implements Consumer<CollectedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(AddNewSfuEntities.class);

    @Inject
    ObserverConfig observerConfig;

    @Inject
    Provider<RefreshSfusTask> refreshTaskProvider;

    @Inject
    Provider<AddSFUsTask> addSFUsTaskProvider;

    @Inject
    Provider<AddSfuTransportsTask> addSfuTransportsTaskProvider;

    @Inject
    Provider<AddSfuRtpPadsTask> addSfuRtpPadsTaskProvider;

    @Override
    @Timed(value = ExposedMetrics.OBSERVERTC_EVALUATORS_ADD_NEW_SFU_ENTITIES_EXECUTION_TIME)
    public void accept(CollectedSfuSamples collectedSfuSamples) throws Throwable {
        Set<UUID> sfuIds = collectedSfuSamples.getSfuIds();
        Set<UUID> transportIds = collectedSfuSamples.getTransportIds();
        Set<UUID> rtpPadIds = new HashSet<>();
        rtpPadIds.addAll(collectedSfuSamples.getOutboundRtpPadIds());
        rtpPadIds.addAll(collectedSfuSamples.getInboundRtpPadIds());
        RefreshSfusTask refreshCallsTask = refreshTaskProvider.get()
                .withSfuIds(sfuIds)
                .withSfuTransportIds(transportIds)
                .withSfuRtpPadIds(rtpPadIds);
        if (!refreshCallsTask.execute().succeeded()) {
            logger.warn("Unsuccessful execution of {}. Entities are not refreshed, new entities are not identified!", RefreshCallsTask.class.getSimpleName());
            return;
        }

        Map<UUID, SfuDTO> newSFUs = new HashMap<>();
        Map<UUID, SfuTransportDTO> newTransports = new HashMap<>();
        Map<UUID, SfuRtpPadDTO> newRtpPads = new HashMap<>();
        RefreshSfusTask.Report report = refreshCallsTask.getResult();
        for (SfuSamples sfuSamples : collectedSfuSamples) {
            var sfuId = sfuSamples.getSfuId();
            for (ObservedSfuSample observedSfuSample : sfuSamples) {
                SfuSample sfuSample = observedSfuSample.getSfuSample();
                if (!report.foundSfuIds.contains(sfuId) && !newSFUs.containsKey(sfuId)) {
                    var sfuDTO = SfuDTO.builder()
                            .withSfuId(sfuId)
                            .withConnectedTimestamp(observedSfuSample.getTimestamp())
                            .withTimeZoneId(observedSfuSample.getTimeZoneId())
                            .withServiceId(observedSfuSample.getServiceId())
                            .withMediaUnitId(observedSfuSample.getMediaUnitId())
                            .build();
                    newSFUs.put(sfuId, sfuDTO);
                }
                SfuSampleVisitor.streamTransports(sfuSample).forEach(sfuTransport -> {
                    UUID transportId = UUID.fromString(sfuTransport.transportId);
                    if (!report.foundSfuTransportIds.contains(transportId) && !newTransports.containsKey(transportId)) {
                        var sfuTransportDTO = SfuTransportDTO.builder()
                                .withSfuId(sfuId)
                                .withTransportId(transportId)
                                .withInternal(sfuTransport.internal)
                                .withServiceId(observedSfuSample.getServiceId())
                                .withMediaUnitId(observedSfuSample.getMediaUnitId())
                                .withOpenedTimestamp(observedSfuSample.getTimestamp())
                                .build();
                        newTransports.put(transportId, sfuTransportDTO);
                    }
                });
                SfuSampleVisitor.streamOutboundRtpPads(sfuSample).forEach(sfuRtpSource -> {
                    UUID streamId = UUID.fromString(sfuRtpSource.rtpStreamId);
                    UUID padId = UUID.fromString(sfuRtpSource.padId);
                    boolean internalPad = Objects.nonNull(sfuRtpSource.internal);
                    if (!report.foundRtpPadIds.contains(padId) && !newRtpPads.containsKey(padId)) {
                        UUID transportId = UUID.fromString(sfuRtpSource.transportId);
                        var sfuRtpPadDTO = SfuRtpPadDTO.builder()
                                .withSfuId(sfuId)
                                .withSfuTransportId(transportId)
                                .withRtpStreamId(streamId)
                                .withSfuRtpPadId(padId)
                                .withInternalPad(internalPad)
                                .withServiceId(observedSfuSample.getServiceId())
                                .withMediaUnitId(observedSfuSample.getMediaUnitId())
                                .withAddedTimestamp(observedSfuSample.getTimestamp())
                                .withStreamDirection(StreamDirection.OUTBOUND)
                                .build();
                        newRtpPads.put(padId, sfuRtpPadDTO);
                    }
                });
                SfuSampleVisitor.streamInboundRtpPads(sfuSample).forEach(sfuRtpSink -> {
                    UUID streamId = UUID.fromString(sfuRtpSink.rtpStreamId);
                    UUID padId = UUID.fromString(sfuRtpSink.padId);
                    if (!report.foundRtpPadIds.contains(padId) && !newRtpPads.containsKey(padId)) {
                        UUID transportId = UUID.fromString(sfuRtpSink.transportId);
                        boolean internalPad = Objects.nonNull(sfuRtpSink.outboundPadId);
                        var sfuRtpPadDTO = SfuRtpPadDTO.builder()
                                .withSfuId(sfuId)
                                .withSfuTransportId(transportId)
                                .withRtpStreamId(streamId)
                                .withSfuRtpPadId(padId)
                                .withInternalPad(internalPad)
                                .withServiceId(observedSfuSample.getServiceId())
                                .withMediaUnitId(observedSfuSample.getMediaUnitId())
                                .withAddedTimestamp(observedSfuSample.getTimestamp())
                                .withStreamDirection(StreamDirection.INBOUND)
                                .build();
                        newRtpPads.put(padId, sfuRtpPadDTO);
                    }
                });
            }
        }
        if (0 < newSFUs.size()) {
            this.addNewSfus(newSFUs);
        }
        if (0 < newTransports.size()) {
            this.addNewTransports(newTransports);
        }
        if (0 < newRtpPads.size()) {
            this.addNewRtpPads(newRtpPads);
        }
    }

    private void addNewSfus(Map<UUID, SfuDTO> DTOs) {
        var task = addSFUsTaskProvider.get()
                .withSfuDTOs(DTOs)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }
    }

    private void addNewTransports(Map<UUID, SfuTransportDTO> DTOs) {
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTOs(DTOs)
                ;
        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }
    }

    private void addNewRtpPads(Map<UUID, SfuRtpPadDTO> DTOs) {
        var task = addSfuRtpPadsTaskProvider.get()
                .withSfuRtpPadDTOs(DTOs);

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }
    }
}
