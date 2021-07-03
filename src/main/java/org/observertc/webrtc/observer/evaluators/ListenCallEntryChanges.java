package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.UUID;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class ListenCallEntryChanges implements EntryListener<UUID, CallDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenCallEntryChanges.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @PostConstruct
    void setup() {

    }

    @Override
    public void entryAdded(EntryEvent<UUID, CallDTO> event) {
        var addedCallDTO = event.getValue();
        if (Objects.isNull(addedCallDTO)) {
            logger.warn("Created call DTO is null");
            return;
        }
        logger.info("Call is registered with id \"{}\" for service \"{}\" at room \"{}\"", addedCallDTO.callId, addedCallDTO.serviceId, addedCallDTO.roomId);
        var report = CallEventReport.newBuilder()
                .setCallId(addedCallDTO.callId.toString())
                .setServiceId(addedCallDTO.serviceId)
                .setRoomId(addedCallDTO.roomId)
                .setTimestamp(addedCallDTO.started)
                .setName(CallEventType.CALL_STARTED.name())
                .build();

        synchronized (this) {
            this.callEventReportSubject.onNext(report);
        }
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
        var removedCallDTO = event.getOldValue();
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
}
