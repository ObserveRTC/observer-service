package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.micrometer.core.annotation.Timed;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.ClientDTO;
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
import java.util.stream.Collectors;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class ListenClientEntryChanges implements EntryListener<UUID, ClientDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenClientEntryChanges.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    private Subject<ClientLeft> removedClients = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Inject
    Provider<RemoveClientsTask> removeClientsTaskProvider;

    @Inject
    Provider<FetchCallClientsTask> fetchCallClientsTaskProvider;

    @Inject
    Provider<RemoveCallsTask> removeCallsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.removedClients
                .buffer(35, TimeUnit.SECONDS)
                .subscribe(this::removeClients);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, ClientDTO> event) {
        ClientDTO addedClientDTO = event.getValue();
        logger.info("Client with id \"{}\" (userId: {}) is registered and bound to call id\"{}\"",
                addedClientDTO.clientId, addedClientDTO.userId, addedClientDTO.callId);
    }

    @Override
    public void entryEvicted(EntryEvent<UUID, ClientDTO> event) {
        // ignore this, as we are interested in expired
    }

    @Override
    public void entryExpired(EntryEvent<UUID, ClientDTO> event) {
        ClientDTO removedClientDTO = event.getOldValue();
        if (Objects.isNull(removedClientDTO)) {
            logger.warn("Client DTO is expired, but the value is null {}", event.toString());
            return;
        }
        Long estimatedLeave = Instant.now().minusSeconds(this.observerConfig.evaluators.clientMaxIdleTime).toEpochMilli();
        ClientLeft clientLeft = new ClientLeft(removedClientDTO, estimatedLeave);
        synchronized (this) {
            this.removedClients.onNext(clientLeft);
        }
        logger.info("Client with id \"{}\" (userId: {}) is left call id\"{}\"", removedClientDTO.clientId, removedClientDTO.userId, removedClientDTO.callId);
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

    @Timed(value = "observertc-evaluators-remove-clients")
    private void removeClients(List<ClientLeft> input) {
        if (Objects.isNull(input) || input.size() < 1) {
            return;
        }

        var removeClientsTask = removeClientsTaskProvider.get();
        input.stream().filter(Objects::nonNull).forEach(callEnded -> removeClientsTask.addRemovedClientDTO(callEnded.clientDTO));
        if (!removeClientsTask.execute().succeeded()) {
            logger.warn("Remove Client Entities are failed");
            return;
        }
        var builders = removeClientsTask.getResult();
        var reports = builders.stream().map(CallEventReport.Builder::build).collect(Collectors.toList());
        this.forwardCallReports(reports);

        Set<UUID> affectedCallIds = input.stream()
                .map(clientLeft -> clientLeft.clientDTO)
                .filter(Objects::nonNull)
                .map(dto -> dto.callId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        this.removeAbandonedCallIds(affectedCallIds);
    }

    private void removeAbandonedCallIds(Set<UUID> affectedCallIds) {
        var fetchCallsClientIds = fetchCallClientsTaskProvider.get();
        fetchCallsClientIds.whereCallIds(affectedCallIds);
        if (!fetchCallsClientIds.execute().succeeded()) {
            return;
        }
        Set<UUID> abandonedCallIds = new HashSet<>();
        Map<UUID, Set<UUID>> remainedCallClientIds = fetchCallsClientIds.getResult();
        affectedCallIds.stream()
                .forEach(affectedCallId -> {
                    Set<UUID> remainedClientIds = remainedCallClientIds.get(affectedCallId);
                    if (Objects.isNull(remainedClientIds)) {
                        abandonedCallIds.add(affectedCallId);
                    }
                    if (remainedClientIds.size() < 1) {
                        abandonedCallIds.add(affectedCallId);
                    }
                });
        if (abandonedCallIds.size() < 1) {
            return;
        }

        var removeCallsTask = this.removeCallsTaskProvider.get()
                .whereCallIds(abandonedCallIds);

        if (!removeCallsTask.execute().succeeded()) {
            logger.warn("Remove calls task has failed");
        }

        var reports = removeCallsTask.getResult().stream()
                .map(CallEventReport.Builder::build)
                .collect(Collectors.toList());

        this.forwardCallReports(reports);
    }

    private void forwardCallReports(List<CallEventReport> callEventReports) {
        if (Objects.isNull(callEventReports) || callEventReports.size() < 1) {
            return;
        }
        synchronized (this) {
            callEventReports.stream().forEach(this.callEventReportSubject::onNext);
        }
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
