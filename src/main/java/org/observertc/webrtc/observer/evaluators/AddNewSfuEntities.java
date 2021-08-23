package org.observertc.webrtc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.dto.*;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.repositories.tasks.*;
import org.observertc.webrtc.observer.samples.*;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class AddNewSfuEntities implements Consumer<CollectedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(AddNewSfuEntities.class);

    private Subject<SfuEventReport> callEventReportSubject = PublishSubject.create();

    public Observable<SfuEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Inject
    Provider<RefreshSfusTask> refreshTaskProvider;

    @Inject
    Provider<AddSFUsTask> addSFUsTaskProvider;

    @Inject
    Provider<AddSfuTransportsTask> addSfuTransportsTaskProvider;

    @Inject
    Provider<AddSfuRtpStreamPodsTask> addSfuRtpStreamPodsTaskProvider;

    @Override
    @Timed(value = ExposedMetrics.OBSERVERTC_EVALUATORS_ADD_NEW_SFU_ENTITIES_EXECUTION_TIME)
    public void accept(CollectedSfuSamples collectedSfuSamples) throws Throwable {
        Set<UUID> sfuIds = collectedSfuSamples.getSfuIds();
        Set<UUID> transportIds = collectedSfuSamples.getTransportIds();
        Set<UUID> rtpPodIds = new HashSet<>();
        rtpPodIds.addAll(collectedSfuSamples.getRtpSourceIds());
        rtpPodIds.addAll(collectedSfuSamples.getRtpSinkIds());
        RefreshSfusTask refreshCallsTask = refreshTaskProvider.get()
                .withSfuIds(sfuIds)
                .withSfuTransportIds(transportIds)
                .withSfuRtpPodIds(rtpPodIds);
        if (!refreshCallsTask.execute().succeeded()) {
            logger.warn("Unsuccessful execution of {}. Entities are not refreshed, new entities are not identified!", RefreshCallsTask.class.getSimpleName());
            return;
        }

        Map<UUID, SfuDTO> newSFUs = new HashMap<>();
        Map<UUID, SfuTransportDTO> newTransports = new HashMap<>();
        Map<UUID, SfuRtpStreamPodDTO> newRtpPods = new HashMap<>();
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
                                .withMediaUnitId(observedSfuSample.getMediaUnitId())
                                .withOpenedTimestamp(observedSfuSample.getTimestamp())
                                .build();
                        newTransports.put(transportId, sfuTransportDTO);
                    }
                });
                SfuSampleVisitor.streamRtpSources(sfuSample).forEach(sfuRtpSource -> {
                    UUID streamId = UUID.fromString(sfuRtpSource.streamId);
                    UUID sourceId = UUID.fromString(sfuRtpSource.sourceId);
                    if (!report.foundRtpPodIds.contains(sourceId) && !newRtpPods.containsKey(sourceId)) {
                        UUID transportId = UUID.fromString(sfuRtpSource.transportId);
                        var sfuRtpStreamDTO = SfuRtpStreamPodDTO.builder()
                                .withSfuId(sfuId)
                                .withSfuTransportId(transportId)
                                .withSfuStreamId(streamId)
                                .withSfuPodId(sourceId)
                                .withMediaUnitId(observedSfuSample.getMediaUnitId())
                                .withAddedTimestamp(observedSfuSample.getTimestamp())
                                .withSfuPodRole(SfuPodRole.SOURCE)
                                .build();
                        newRtpPods.put(sourceId, sfuRtpStreamDTO);
                    }
                });
                SfuSampleVisitor.streamRtpSinks(sfuSample).forEach(sfuRtpSink -> {
                    UUID streamId = UUID.fromString(sfuRtpSink.streamId);
                    UUID sinkId = UUID.fromString(sfuRtpSink.sinkId);
                    if (!report.foundRtpPodIds.contains(streamId) && !newRtpPods.containsKey(sinkId)) {
                        UUID transportId = UUID.fromString(sfuRtpSink.transportId);
                        var sfuRtpStreamDTO = SfuRtpStreamPodDTO.builder()
                                .withSfuId(sfuId)
                                .withSfuTransportId(transportId)
                                .withSfuStreamId(streamId)
                                .withSfuPodId(sinkId)
                                .withMediaUnitId(observedSfuSample.getMediaUnitId())
                                .withAddedTimestamp(observedSfuSample.getTimestamp())
                                .withSfuPodRole(SfuPodRole.SINK)
                                .build();
                        newRtpPods.put(sinkId, sfuRtpStreamDTO);
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
        if (0 < newRtpPods.size()) {
            this.addNewRtpStreamPods(newRtpPods);
        }
    }

    private void addNewRtpStreamPods(Map<UUID, SfuRtpStreamPodDTO> DTOs) {
        var task = addSfuRtpStreamPodsTaskProvider.get()
                .withSfuRtpStreamDTOs(DTOs);

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }

        List<SfuEventReport> reports = task.getResult().stream()
                .map(builder -> builder.setMessage("Sfu RTP stream is added"))
                .map(this::buildReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.forwardReports(reports);
    }

    private void addNewTransports(Map<UUID, SfuTransportDTO> DTOs) {
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTOs(DTOs)
                ;
        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }

        List<SfuEventReport> reports = task.getResult().stream()
                .map(builder -> builder.setMessage("Sfu Transport is added"))
                .map(this::buildReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.forwardReports(reports);
    }

    private void addNewSfus(Map<UUID, SfuDTO> DTOs) {
        var task = addSFUsTaskProvider.get()
                .withSfuDTOs(DTOs)
        ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }
        List<SfuEventReport> reports = task.getResult().stream()
                .map(builder -> builder.setMessage("New SFU is started"))
                .map(this::buildReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.forwardReports(reports);
    }

    private void forwardReports(List<SfuEventReport> reports) {
        if (Objects.isNull(reports) || reports.size() < 1) {
            return;
        }
        synchronized (this) {
            reports.stream().filter(Objects::nonNull).forEach(this.callEventReportSubject::onNext);
        }
    }

    private SfuEventReport buildReport(SfuEventReport.Builder builder) {
        try {
            return builder.build();
        } catch (Exception ex) {
            logger.warn("Cannot build report due to exception", ex);
            return null;
        }
    }
}
