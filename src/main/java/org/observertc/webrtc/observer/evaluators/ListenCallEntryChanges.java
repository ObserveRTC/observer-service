package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.repositories.tasks.CreateCallEventReportsTaskProvider;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class ListenCallEntryChanges implements EntryListener<UUID, CallDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenCallEntryChanges.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    private Subject<CallDTO> addedCalls = PublishSubject.create();
    private Subject<CallEnded> removedCalls = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Inject
    CreateCallEventReportsTaskProvider createCallEventReportsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.addedCalls
                .buffer(10, TimeUnit.SECONDS)
                .subscribe(this::addCalls);

        this.removedCalls
                .buffer(115, TimeUnit.SECONDS)
                .subscribe(this::removeCalls);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, CallDTO> event) {
        CallDTO addedCallDTO = event.getValue();
        synchronized (this) {
            this.addedCalls.onNext(addedCallDTO);
        }
        logger.info("Call is registered with id \"{}\" for service \"{}\" at room \"{}\"", addedCallDTO.callId, addedCallDTO.serviceId, addedCallDTO.roomId);
    }

    @Override
    public void entryEvicted(EntryEvent<UUID, CallDTO> event) {
        logger.warn("CallDTO {} has been evicted?! Should not happen", event.getValue());
    }

    @Override
    public void entryExpired(EntryEvent<UUID, CallDTO> event) {
        logger.warn("CallDTO {} has been expired?! Should not happen", event.getValue());
    }

    @Override
    public void entryRemoved(EntryEvent<UUID, CallDTO> event) {
        CallDTO removedCallDTO = event.getOldValue();
        if (Objects.isNull(removedCallDTO)) {
            logger.warn("Call DTO is removed, but the removed value is null {}", event.toString());
            return;
        }
        Long estimatedLeave = Instant.now().minusSeconds(this.observerConfig.evaluators.clientMaxIdleTime).toEpochMilli();
        CallEnded callEnded = new CallEnded(removedCallDTO, estimatedLeave);
        synchronized (this) {
            this.removedCalls.onNext(callEnded);
        }
        logger.info("Call with id \"{}\" for service \"{}\" at room \"{}\" is removed", removedCallDTO.callId, removedCallDTO.serviceId, removedCallDTO.roomId);
    }

    @Override
    public void entryUpdated(EntryEvent<UUID, CallDTO> event) {
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

    private void addCalls(List<CallDTO> addedCallDTOs) {
        if (Objects.isNull(addedCallDTOs) || addedCallDTOs.size() < 1) {
            return;
        }

        var task = this.createCallEventReportsTaskProvider.getCreateCallStartedReportsTask();
        addedCallDTOs.forEach(task::withDTO);

        if (!task.execute().succeeded()) {
            logger.warn("Making add client report has been failed");
            return;
        }
        this.forwardCallReports(
                task.getResult()
        );
    }

    private void removeCalls(List<CallEnded> input) {
        if (Objects.isNull(input) || input.size() < 1) {
            return;
        }
        // NOTE: We do not remove the call here, because it was ordered in the clientListener

        var createReportsTask = this.createCallEventReportsTaskProvider.getCreateCallEndedReportsTask();
        input.stream().filter(Objects::nonNull).forEach(clientLeft -> createReportsTask.withDTOAndTimestamp(clientLeft.callDTO, clientLeft.estimatedLeave));

        if (!createReportsTask.execute().succeeded()) {
            logger.warn("Making add client report has been failed");
            return;
        }
        this.forwardCallReports(
                createReportsTask.getResult()
        );
    }

    private void forwardCallReports(List<CallEventReport> callEventReports) {
        if (Objects.isNull(callEventReports) || callEventReports.size() < 1) {
            return;
        }
        synchronized (this) {
            callEventReports.stream().forEach(this.callEventReportSubject::onNext);
        }
    }


    private class CallEnded {
        private final CallDTO callDTO;
        private final Long estimatedLeave;

        private CallEnded(CallDTO clientDTO, Long estimatedLeave) {
            this.callDTO = clientDTO;
            this.estimatedLeave = estimatedLeave;
        }
    }
}
