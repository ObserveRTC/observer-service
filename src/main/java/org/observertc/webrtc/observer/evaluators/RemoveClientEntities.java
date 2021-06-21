package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class RemoveClientEntities implements EntryListener<UUID, ClientDTO> {
    private static final Logger logger = LoggerFactory.getLogger(RemoveClientEntities.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Override
    public void entryAdded(EntryEvent<UUID, ClientDTO> event) {

    }

    @Override
    public void entryEvicted(EntryEvent<UUID, ClientDTO> event) {

    }

    @Override
    public void entryExpired(EntryEvent<UUID, ClientDTO> event) {

    }

    @Override
    public void entryRemoved(EntryEvent<UUID, ClientDTO> event) {

    }

    @Override
    public void entryUpdated(EntryEvent<UUID, ClientDTO> event) {

    }

    @Override
    public void mapCleared(MapEvent event) {

    }

    @Override
    public void mapEvicted(MapEvent event) {

    }
}
