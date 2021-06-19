package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.observertc.webrtc.observer.repositories.tasks.RefreshTask;
import org.observertc.webrtc.observer.samples.ObservedSamples;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;
import java.util.UUID;

/**
 * Responsible to Join Clients to Calls
 */
@Prototype
public class UpdateClientsOperator implements ObservableOperator<CallEventReport, ObservedSamples> {
    private static final Logger logger = LoggerFactory.getLogger(UpdateClientsOperator.class);

    @Inject
    Provider<RefreshTask> refreshTaskProvider;

    @PostConstruct
    void setup() {

    }

    @Override
    public @NonNull Observer<? super ObservedSamples> apply(@NonNull Observer<? super CallEventReport> observer) throws Throwable {
        return new Operator(observer);
    }

    private class Operator implements Observer<ObservedSamples>{
        private final Observer<? super CallEventReport> observer;

        private Operator(Observer<? super CallEventReport> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onNext(@NonNull ObservedSamples observedSamples) {
            Set<UUID> receivedPeerConnectionIds = observedSamples.getPeerConnectionIds();
            Set<UUID> receivedClientIds = observedSamples.getClientIds();
            Set<String> receivedMediaTrackKeys = observedSamples.getMediaTrackKeys();
            var task = refreshTaskProvider.get();
            task.withClientIds(receivedClientIds)
                    .withPeerConnectionIds(receivedPeerConnectionIds)
                    .withMediaTrackKeys(receivedMediaTrackKeys);

            if (!task.execute().succeeded()) {
                // TODO: handle this
                return;
            }

            RefreshTask.Report report = task.getResult();
            for (UUID clientId : receivedClientIds) {
                if (report.foundClientIds.contains(clientId)) {
                    continue;
                }
                // TODO: Add new client
            }

            for (UUID peerConnectionId : receivedPeerConnectionIds) {
                if (report.foundPeerConnectionIdsToClientIds.containsKey(peerConnectionId)) {
                    continue;
                }
                // TODO: Add new peer connection
            }

            for (String mediaTrackKey : receivedMediaTrackKeys) {
                if (report.foundPeerConnectionIdsToClientIds.containsKey(peerConnectionId)) {
                    continue;
                }
                // TODO: Add new peer connection
            }

        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    }

}
