package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.models.ICEConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.ICEConnectionsRepository;
import org.observertc.webrtc.observer.tasks.ICEConnectionAdderTask;
import org.observertc.webrtc.observer.tasks.ICEConnectionRemoverTask;
import org.observertc.webrtc.observer.tasks.ICEConnectionUpdaterTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class ICEConnectionsEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ICEConnectionsEvaluator.class);
    private Subject<ICEConnectionEntity> newICEConnection = PublishSubject.create();
    private Subject<ICEConnectionEntity> updatedICEConnection = PublishSubject.create();
    private Subject<UUID> expiredPC = PublishSubject.create();
    private Subject<ICECandidatePairUpdate> expiredCandidatePairs = PublishSubject.create();
    private final FlawMonitor flawMonitor;

    @Inject
    TasksProvider tasksProvider;

    public ICEConnectionsEvaluator(
            MonitorProvider monitorProvider
    ) {
        this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
        this.newICEConnection
                .buffer(10, TimeUnit.SECONDS)
                .subscribe(this::processICEConnections);

        this.updatedICEConnection
                .buffer(30, TimeUnit.SECONDS)
                .subscribe(this::processUpdatedICEConnectionUpdates);

        this.expiredPC
                .buffer(10, TimeUnit.SECONDS)
                .subscribe(this::processExpiredPCs);

        this.expiredCandidatePairs
                .buffer(10, TimeUnit.SECONDS)
                .subscribe(this::processExpiredICEConnectionUpdates);
    }


    public Observer<ICEConnectionEntity> getNewICEConnectionsInput() {
        return this.newICEConnection;
    }

    public Observer<UUID> getExpiredPCsInput() {
        return this.expiredPC;
    }

    public Observer<ICECandidatePairUpdate> getExpiredICECandidatePairs() {
        return this.expiredCandidatePairs;
    }

    public Observer<? super ICEConnectionEntity> getUpdatedICEConnectionsInput() {
        return this.updatedICEConnection;
    }

    private void processICEConnections(List<ICEConnectionEntity> iceConnectionEntities) {
        if (Objects.isNull(iceConnectionEntities)) {
            return;
        }
        if (iceConnectionEntities.size() < 1) {
            return;
        }
        ICEConnectionAdderTask task = this.tasksProvider.provideICEConnectionAdderTask();
        iceConnectionEntities.forEach(task::forICEConnectionEntity);
        task
                .withLogger(logger)
                .withFlawMonitor(this.flawMonitor)
        ;

        if (!task.execute().succeeded()) {
            return;
        }
    }



    private void processExpiredPCs(List<UUID> pcUUIDs) {
        if (Objects.isNull(pcUUIDs)) {
            return;
        }
        if (pcUUIDs.size() < 1) {
            return;
        }
        ICEConnectionRemoverTask task =
                this.tasksProvider.provideICEConnectionRemoverTask()
                        .forPcUUIDs(pcUUIDs);
        task.withExceptionMessage(() ->
                MessageFormatter.format("Cannot remove ICE connection belongs to peer connections ({}), due to exception", ObjectToString.toString(pcUUIDs)).getMessage()
        )
                .withFlawMonitor(this.flawMonitor)
                .withLogger(logger)
                .execute();

        if (!task.succeeded()) {
            return;
        }
    }

    private void processUpdatedICEConnectionUpdates(List<ICEConnectionEntity> updates) {
        if (Objects.isNull(updates)) {
            return;
        }
        if (updates.size() < 1) {
            return;
        }
        Map<String, ICEConnectionEntity> updateMaps = new HashMap<>();
        Iterator<ICEConnectionEntity> it = updates.listIterator();
        while (it.hasNext()) {
            ICEConnectionEntity entity = it.next();
            String key = ICEConnectionsRepository.getKey(entity.pcUUID, entity.localCandidateId, entity.remoteCandidateId);
            updateMaps.put(key, entity); // the newer update overrides the old one
        }

        ICEConnectionUpdaterTask task = this.tasksProvider.provideICEConnectionUpdaterTask();
        updateMaps.values().stream().forEach(task::forICEConnectionEntity);

        task.withLogger(logger)
                .withFlawMonitor(this.flawMonitor)
                .execute();

        if (!task.succeeded()) {
            return;
        }
    }

    private void processExpiredICEConnectionUpdates(List<ICECandidatePairUpdate> updates) {
        if (Objects.isNull(updates)) {
            return;
        }
        if (updates.size() < 1) {
            return;
        }
        Set<String> keys = updates.stream().map(
                update -> ICEConnectionsRepository.getKey(update.pcUUID, update.localCandidateId, update.remoteCandidateId)
        ).collect(Collectors.toSet());

        ICEConnectionRemoverTask task =
                this.tasksProvider.provideICEConnectionRemoverTask()
                        .forICEConnectionKeys(keys);
        task.withExceptionMessage(() ->
                MessageFormatter.format("Cannot remove ICE connection belongs to keys ({}), due to exception", ObjectToString.toString(keys)).getMessage()
        )
                .withFlawMonitor(this.flawMonitor)
                .withLogger(logger)
                .execute();

        if (!task.succeeded()) {
            return;
        }
    }

}
