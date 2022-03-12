package org.observertc.observer.repositories;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.common.BufferUtils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.repositories.tasks.RemoveSfuRtpPadsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class SfuRtpPadEvents {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadEvents.class);

    public static class Payload {
        public SfuRtpPadDTO sfuRtpPadDTO;
        public SfuStreamDTO sfuStreamDTO;
        public SfuSinkDTO sfuSinkDTO;
        public Long timestamp;
    }

    private static Payload createPayload(SfuRtpPadDTO rtpPadDTO, SfuStreamDTO streamDTO, SfuSinkDTO sinkDTO, Long timestamp) {
        var result = new Payload();
        result.sfuRtpPadDTO = rtpPadDTO;
        result.sfuStreamDTO = streamDTO;
        result.sfuSinkDTO = sinkDTO;
        result.timestamp = timestamp;
        return result;
    }

    private Subject<Payload> completedSfuRtpPad = PublishSubject.create();
    private Subject<Payload> disposedSfuRtpPad = PublishSubject.create();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryEvents repositoryEvents;

//    @Inject
//    Provider<FindTrackIdsForStreamAndSinkIds> findTrackIdsForStreamAndSinkIdsProvider;

    @Inject
    Provider<RemoveSfuRtpPadsTask> removeSfuRtpPadsTaskProvider;

    @PostConstruct
    void setup() {
        this.repositoryEvents.addedSfuRtpPads().subscribe(this::onSfuRtpPadAdded);
        this.repositoryEvents.removedSfuRtpPads().subscribe(list -> this.onSfuRtpPadsRemoved(list, Collections.EMPTY_MAP));
        this.repositoryEvents.expiredSfuRtpPads().subscribe(this::onSfuRtpPadsExpired);
        this.repositoryEvents.updatedSfuStreams().subscribe(this::onSfuStreamUpdated);
        this.repositoryEvents.updatedSfuSinks().subscribe(this::onSfuSinkUpdated);
    }

    public Observable<List<Payload>> completedSfuRtpPads() {
        return this.getObservableList(this.completedSfuRtpPad);
    }

    public Observable<List<Payload>> disposedSfuRtpPads() {
        return this.getObservableList(this.disposedSfuRtpPad);
    }

    private void onSfuRtpPadAdded(List<SfuRtpPadDTO> sfuRtpPads) {
        var streamIds = sfuRtpPads.stream().map(pad -> pad.streamId).collect(Collectors.toSet());
        var sinkIds = sfuRtpPads.stream().map(pad -> pad.sinkId).collect(Collectors.toSet());
        Map<UUID, SfuStreamDTO> streamDTOs;
        if (0 < streamIds.size()) streamDTOs = this.hazelcastMaps.getSfuStreams().getAll(streamIds);
        else streamDTOs = Collections.EMPTY_MAP;
        Map<UUID, SfuSinkDTO> sinkDTOs;
        if (0 < sinkIds.size()) sinkDTOs = this.hazelcastMaps.getSfuSinks().getAll(sinkIds);
        else sinkDTOs = Collections.EMPTY_MAP;
        sfuRtpPads.forEach(sfuRtpPad -> {
            if (Objects.nonNull(sfuRtpPad.sinkId)) {
                var sinkDTO = sinkDTOs.get(sfuRtpPad.sinkId);
                if (Objects.nonNull(sinkDTO) && Objects.nonNull(sinkDTO.trackId) && Objects.nonNull(sinkDTO.sfuTransportId)) {
                    var payload = createPayload(
                            sfuRtpPad,
                            null,
                            sinkDTO,
                            sfuRtpPad.added
                    );
                    completedSfuRtpPad.onNext(payload);
                    return;
                }
                this.hazelcastMaps.getSfuUnboundRtpPadIds().add(sfuRtpPad.rtpPadId);
                return;
            }
            if (Objects.nonNull(sfuRtpPad.streamId)) {
                var streamDTO = streamDTOs.get(sfuRtpPad.streamId);
                if (Objects.nonNull(streamDTO) && Objects.nonNull(streamDTO.trackId) && Objects.nonNull(streamDTO.sfuTransportId)) {
                    var payload = createPayload(
                            sfuRtpPad,
                            streamDTO,
                            null,
                            sfuRtpPad.added
                    );
                    completedSfuRtpPad.onNext(payload);
                    return;
                }
                this.hazelcastMaps.getSfuUnboundRtpPadIds().add(sfuRtpPad.rtpPadId);
                return;
            }
            var payload = createPayload(
                    sfuRtpPad,
                    null,
                    null,
                    sfuRtpPad.added
            );
            this.completedSfuRtpPad.onNext(payload);
        });
    }

    private void onSfuRtpPadsRemoved(List<SfuRtpPadDTO> sfuRtpPads, Map<UUID, Long> timestamps) {
        var streamIds = sfuRtpPads.stream().map(pad -> pad.streamId).collect(Collectors.toSet());
        var sinkIds = sfuRtpPads.stream().map(pad -> pad.sinkId).collect(Collectors.toSet());
        Map<UUID, SfuStreamDTO> streamDTOs;
        if (0 < streamIds.size()) streamDTOs = this.hazelcastMaps.getSfuStreams().getAll(streamIds);
        else streamDTOs = Collections.EMPTY_MAP;
        Map<UUID, SfuSinkDTO> sinkDTOs;
        if (0 < sinkIds.size()) sinkDTOs = this.hazelcastMaps.getSfuSinks().getAll(sinkIds);
        else sinkDTOs = Collections.EMPTY_MAP;
        this.removedSfuRtpPads(sfuRtpPads, streamDTOs, sinkDTOs, timestamps);
    }

    private void onSfuRtpPadsExpired(List<RepositoryExpiredEvent<SfuRtpPadDTO>> expiredSfuRtpPads) {
        var sfuRtpPads = expiredSfuRtpPads.stream().map(event -> event.getValue()).collect(Collectors.toList());
        var timestamps = expiredSfuRtpPads.stream().collect(Collectors.toMap(
                event -> event.getValue().rtpPadId,
                event -> event.estimatedLastTouch()
        ));
        this.onSfuRtpPadsRemoved(sfuRtpPads, timestamps);

        var task = this.removeSfuRtpPadsTaskProvider.get();
        sfuRtpPads.forEach(task::addRemovedSfuRtpStreamPadDTO);
        if (!task.execute().succeeded()) {
            logger.warn("Task {} was not successful", task.getName());
        }
    }

    private void removedSfuRtpPads(List<SfuRtpPadDTO> sfuRtpPads,
                                   Map<UUID, SfuStreamDTO> streamDTOs,
                                   Map<UUID, SfuSinkDTO> sinkDTOs,
                                   Map<UUID, Long> timestamps) {
        sfuRtpPads.forEach(sfuRtpPad -> {
            var streamDTO = streamDTOs.get(sfuRtpPad.streamId);
            var sinkDTO = sinkDTOs.get(sfuRtpPad.sinkId);
            var incomplete = this.hazelcastMaps.getSfuUnboundRtpPadIds().contains(sfuRtpPad.rtpPadId);
            var now = Instant.now().toEpochMilli();
            if (incomplete) {
                var payload = createPayload(
                        sfuRtpPad,
                        streamDTO,
                        sinkDTO,
                        sfuRtpPad.added
                );
                this.completedSfuRtpPad.onNext(payload);
                this.hazelcastMaps.getSfuUnboundRtpPadIds().remove(sfuRtpPad.rtpPadId);
            }
            var timestamp = timestamps.getOrDefault(sfuRtpPad.rtpPadId, now);
            var payload = createPayload(
                    sfuRtpPad,
                    streamDTO,
                    sinkDTO,
                    timestamp
            );
            this.disposedSfuRtpPad.onNext(payload);
        });
    }

    private void onSfuStreamUpdated(List<RepositoryUpdatedEvent<SfuStreamDTO>> sfuStreams) {
        sfuStreams.forEach(updatedEvent -> {
            var sfuStream = updatedEvent.getNewValue();
            if (Objects.isNull(sfuStream)) return;
            if (Objects.isNull(sfuStream.trackId) || Objects.isNull(sfuStream.sfuTransportId)) {
                // incomplete
                return;
            }
            // completed
            Set<UUID> rtpPadIds = this.hazelcastMaps.getSfuSinkIdToRtpPadIds().get(sfuStream.sfuStreamId).stream().collect(Collectors.toSet());
            var sfuRtpPads = this.hazelcastMaps.getSFURtpPads().getAll(rtpPadIds);
            for (var sfuRtpPad : sfuRtpPads.values()) {
                if (!this.hazelcastMaps.getSfuUnboundRtpPadIds().contains(sfuRtpPad.rtpPadId)) {
                    continue;
                }
                var payload = createPayload(
                        sfuRtpPad,
                        sfuStream,
                        null,
                        sfuRtpPad.added
                );
                this.completedSfuRtpPad.onNext(payload);
                this.hazelcastMaps.getSfuUnboundRtpPadIds().remove(sfuRtpPad.rtpPadId);
            }
        });
    }

    private void onSfuSinkUpdated(List<RepositoryUpdatedEvent<SfuSinkDTO>> sfuSinks) {
        sfuSinks.forEach(updatedEvent -> {
            var sfuSink = updatedEvent.getNewValue();
            if (Objects.isNull(sfuSink)) return;
            if (Objects.isNull(sfuSink.trackId) || Objects.isNull(sfuSink.sfuTransportId)) {
                // incomplete
                return;
            }
            // completed
            Set<UUID> rtpPadIds = this.hazelcastMaps.getSfuSinkIdToRtpPadIds().get(sfuSink.sfuSinkId).stream().collect(Collectors.toSet());
            var sfuRtpPads = this.hazelcastMaps.getSFURtpPads().getAll(rtpPadIds);
            for (var sfuRtpPad : sfuRtpPads.values()) {
                if (!this.hazelcastMaps.getSfuUnboundRtpPadIds().contains(sfuRtpPad.rtpPadId)) {
                    continue;
                }
                var payload = createPayload(
                        sfuRtpPad,
                        null,
                        sfuSink,
                        sfuRtpPad.added
                );
                this.completedSfuRtpPad.onNext(payload);
                this.hazelcastMaps.getSfuUnboundRtpPadIds().remove(sfuRtpPad.rtpPadId);
            }
        });
    }

    private<T> Observable<List<T>> getObservableList(Observable<T> source) {
        var debounceConfig = this.observerConfig.buffers.repositoryEventsDebouncers;
        return BufferUtils.wrapObservable(source, debounceConfig);
    }
}
