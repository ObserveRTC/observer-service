package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.repositories.tasks.CreateCallEventReports;
import org.observertc.webrtc.observer.repositories.tasks.FetchCallClientsTask;
import org.observertc.webrtc.observer.repositories.tasks.RemoveCallsTask;
import org.observertc.webrtc.observer.repositories.tasks.RemoveClientsTask;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class ReportClientChanges implements EntryListener<UUID, ClientDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ReportClientChanges.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    private Subject<ClientDTO> addedClients = PublishSubject.create();
    private Subject<ClientLeft> removedClients = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Inject
    Provider<RemoveClientsTask> removeClientsTaskProvider;

    @Inject
    Provider<CreateCallEventReports> createCallEventReportsProvider;

    @Inject
    Provider<FetchCallClientsTask> fetchCallClientsTaskProvider;

    @Inject
    Provider<RemoveCallsTask> removeCallsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.addedClients
                .buffer(30, TimeUnit.SECONDS)
                .subscribe();

        this.removedClients
                .buffer(30, TimeUnit.SECONDS)
                .subscribe(this::removeClients);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, ClientDTO> event) {
        ClientDTO addedClientDTO = event.getValue();
        synchronized (this) {
            this.addedClients.onNext(addedClientDTO);
        }
    }

    @Override
    public void entryEvicted(EntryEvent<UUID, ClientDTO> event) {
        // ignore this, as we are interested in expired
    }

    @Override
    public void entryExpired(EntryEvent<UUID, ClientDTO> event) {
        ClientDTO removedClientDTO = event.getValue();
        Long estimatedLeave = Instant.now().minusSeconds(this.observerConfig.evaluators.clientMaxIdleTime).toEpochMilli();
        ClientLeft clientLeft = new ClientLeft(removedClientDTO, estimatedLeave);
        synchronized (this) {
            this.removedClients.onNext(clientLeft);
        }
    }

    @Override
    public void entryRemoved(EntryEvent<UUID, ClientDTO> event) {
        logger.info("ClientDTO {} has been removed", event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent<UUID, ClientDTO> event) {
        // ignore this event
    }

    @Override
    public void mapCleared(MapEvent event) {
        logger.info("Source map has been cleared, {} items are removed", event.getNumberOfEntriesAffected());
    }

    @Override
    public void mapEvicted(MapEvent event) {
        logger.info("Source map has been evicted, {} items are removed", event.getNumberOfEntriesAffected());
    }

    private void addClients(List<ClientDTO> addedClientDTOs) {
        if (Objects.isNull(addedClientDTOs) || addedClientDTOs.size() < 1) {
            return;
        }
        var task = createCallEventReportsProvider.get()
                .withCallEventType(CallEventType.CLIENT_JOINED)
                .withCallEventMessage("Client has joined");
        addedClientDTOs.stream().forEach(task::withClientDTO);

        if (!task.execute().succeeded()) {
            logger.warn("Making add client report has been failed");
            return;
        }

        List<CallEventReport> joinedClientReports = task.getResult();
        if (joinedClientReports.size() < 1) {
            return;
        }
        synchronized (this) {
            joinedClientReports.stream().forEach(this.callEventReportSubject::onNext);
        }
    }

    private void removeClients(List<ClientLeft> input) {
        if (Objects.isNull(input) || input.size() < 1) {
            return;
        }
        Map<UUID, ClientLeft> leftClients = input.stream()
                .collect(Collectors.toMap(
                        clientLeft -> clientLeft.clientDTO.clientId,
                        Function.identity()
                ));
        List<CallEventReport> leftClientReports = this.makeClientLeftReports(leftClients);
        if (leftClientReports.size() < 1) {
            return;
        }
        synchronized (this) {
            leftClientReports.stream().forEach(this.callEventReportSubject::onNext);
        }
        Set<UUID> affectedCallIds = leftClientReports.stream()
                .map(CallEventReport::getCallId)
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        Set<UUID> abandonedCallIds = this.getAbandonedCallIds(affectedCallIds);
        if (abandonedCallIds.size() < 1) {
            return;
        }
        List<CallEventReport> endedCallReports = makeCallEndedReports(abandonedCallIds);
        if (endedCallReports.size() < 1) {
            return;
        }
        synchronized (this) {
            endedCallReports.stream().forEach(this.callEventReportSubject::onNext);
        }
    }

    private List<CallEventReport> makeClientLeftReports(Map<UUID, ClientLeft> leftClients) {
        if (Objects.isNull(leftClients) || leftClients.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        ChainedTask<List<CallEventReport>> task = ChainedTask.<List<CallEventReport>>builder()
                .<Map<UUID, ClientEntity>>addSupplierStage("Remove Client Entities from repositories", () -> {
                    var removeClientsTask = removeClientsTaskProvider.get();
                    leftClients.values().stream().map(i -> i.clientDTO).forEach(removeClientsTask::addRemovedClientDTO);
                    if (!removeClientsTask.execute().succeeded()) {
                        return Collections.EMPTY_MAP;
                    }
                    return removeClientsTask.getResult();
                })
                .<Map<UUID, ClientEntity>>addBreakCondition((clientEntitiesObj, resultHolder) -> {
                    Map<UUID, ClientEntity> clientEntities = (Map<UUID, ClientEntity>) clientEntitiesObj;
                    if (Objects.isNull(clientEntities) || clientEntities.size() < 1) {
                        // if we were not able to remove the clients
                        return true;
                    }
                    return false;
                })
                .<Map<UUID, ClientEntity>, List<CallEventReport>>addFunctionalStage("Make reports about the clients removed",
                        removedClientEntitiesObj -> {
                            Map<UUID, ClientEntity> removedClientEntities = (Map<UUID, ClientEntity>)removedClientEntitiesObj;
                            var makeReportsTask = createCallEventReportsProvider.get()
                                    .withCallEventType(CallEventType.CLIENT_LEFT)
                                    .withCallEventMessage("Client has left the room")
                                    ;

                            removedClientEntities.keySet()
                                    .stream()
                                    .map(leftClients::get)
                                    .filter(Objects::nonNull)
                                    .forEach(clientLeft -> makeReportsTask.withClientDTO(clientLeft.clientDTO, clientLeft.estimatedLeave));
                            if (!makeReportsTask.execute().succeeded()) {
                                return Collections.EMPTY_LIST;
                            }
                            return makeReportsTask.getResult();
                        })
                .build();
        if (!task.execute().succeeded()) {
            return Collections.EMPTY_LIST;
        }

        return task.getResult();
    }

    private Set<UUID> getAbandonedCallIds(Set<UUID> affectedCallIds) {
        var fetchCallsClientIds = fetchCallClientsTaskProvider.get();
        fetchCallsClientIds.whereCallIds(affectedCallIds);
        if (!fetchCallsClientIds.execute().succeeded()) {
            return Collections.EMPTY_SET;
        }
        Set<UUID> result = new HashSet<>();
        Map<UUID, Set<UUID>> remainedCallClientIds = fetchCallsClientIds.getResult();
        affectedCallIds.stream()
                .forEach(affectedCallId -> {
                    Set<UUID> remainedClientIds = remainedCallClientIds.get(affectedCallId);
                    if (Objects.isNull(remainedClientIds)) {
                        result.add(affectedCallId);
                    }
                    if (remainedClientIds.contains(affectedCallId)) {
                        // TODO: inconsistency!
                        if (remainedClientIds.size() == 1) {
                            result.add(affectedCallId);
                        }
                    }
                });
        return result;
    }


    private List<CallEventReport> makeCallEndedReports(Set<UUID> abandonedCallIds) {
        if (Objects.isNull(abandonedCallIds) || abandonedCallIds.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        ChainedTask<List<CallEventReport>> task = ChainedTask.<List<CallEventReport>>builder()
                .<Map<UUID, CallEntity>>addSupplierStage("Remove Call Entities from repositories", () -> {
                    var removeCallsTask = removeCallsTaskProvider.get()
                            .whereCallIds(abandonedCallIds);
                    if (!removeCallsTask.execute().succeeded()) {
                        return Collections.EMPTY_MAP;
                    }
                    return removeCallsTask.getResult();
                })
                .<Map<UUID, CallEntity>>addBreakCondition((callEntitiesObj, resultHolder) -> {
                    Map<UUID, CallEntity> callEntities = (Map<UUID, CallEntity>)callEntitiesObj;
                    if (Objects.isNull(callEntities) || callEntities.size() < 1) {
                        // if we were not able to remove the clients
                        return true;
                    }
                    return false;
                })
                .<Map<UUID, CallEntity>, List<CallEventReport>>addFunctionalStage("Make reports about the ended calls",
                        removedCallEntitiesInput -> {
                            Map<UUID, CallEntity> removedCallEntities = (Map<UUID, CallEntity>) removedCallEntitiesInput;
                            var makeReportsTask = createCallEventReportsProvider.get()
                                    .withCallEventType(CallEventType.CALL_ENDED)
                                    .withCallEventMessage("Call has been ended")
                                    ;
                            removedCallEntities.values().stream()
                                    .forEach(callEntity -> makeReportsTask.withCallDTO(callEntity.getCallDTO()));
                            if (!makeReportsTask.execute().succeeded()) {
                                return Collections.EMPTY_LIST;
                            }
                            return makeReportsTask.getResult();
                        })
                .build();
        if (!task.execute().succeeded()) {
            return Collections.EMPTY_LIST;
        }

        return task.getResult();
    }

    private class ClientLeft {
        private final ClientDTO clientDTO;
        private final Long estimatedLeave;

        private ClientLeft(ClientDTO clientDTO, Long estimatedLeave) {
            this.clientDTO = clientDTO;
            this.estimatedLeave = estimatedLeave;
        }
    }
}
