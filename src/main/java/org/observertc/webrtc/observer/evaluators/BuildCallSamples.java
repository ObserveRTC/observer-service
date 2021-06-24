package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.observertc.webrtc.observer.repositories.tasks.CreateCallIfNotExistsTask;
import org.observertc.webrtc.observer.repositories.tasks.FindCallIdsByServiceRoomIds;
import org.observertc.webrtc.observer.samples.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsibilities:
 *  - group collected call samples into service room ids and assign callId to
 *  each group. The Group then form as a CallSample, and a builders are forwarded for further
 *
 */
@Prototype
public class BuildCallSamples implements ObservableOperator<CollectedCallSamples, CollectedClientSamples> {

    private static final Logger logger = LoggerFactory.getLogger(BuildCallSamples.class);

    @Inject
    Provider<CreateCallIfNotExistsTask> createCallIfNotExistsTaskProvider;

    @Inject
    Provider<FindCallIdsByServiceRoomIds> findCallsTaskProvider;


    @PostConstruct
    void setup() {

    }

    @Override
    public @NonNull Observer<? super CollectedClientSamples> apply(@NonNull Observer<? super CollectedCallSamples> observer) throws Throwable {
        return new Op(observer);
    }

    private class Op implements Observer<CollectedClientSamples>{
        final Observer<? super CollectedCallSamples> observer;

        Op(Observer<? super CollectedCallSamples> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onNext(@NonNull CollectedClientSamples collectedClientSamples) {
            Set<ServiceRoomId> serviceRoomIds = collectedClientSamples.getServiceRoomIds();
            var findCallsTask = findCallsTaskProvider.get()
                    .whereServiceRoomIds(serviceRoomIds)
                    .withUnmodifiableResult(false);

            if (!findCallsTask.execute().succeeded()) {
                return;
            }
            var serviceRoomIdsToCallIds = findCallsTask.getResult();

            CollectedCallSamples.Builder collectedCallSamplesBuilder = CollectedCallSamples.builder();
            for (ServiceRoomId serviceRoomId : serviceRoomIds) {
                List<ClientSamples> clientSamplesList = collectedClientSamples
                        .streamByServiceRoomId(serviceRoomId)
                        .collect(Collectors.toList());

                UUID callId = serviceRoomIdsToCallIds.get(serviceRoomId);
                if (Objects.isNull(callId)) {
                    callId = this.createCallIfNotExists(serviceRoomId, clientSamplesList);
                    if (Objects.isNull(callId)) {
                        logger.warn("Cannot assign serviceRoom to call. all related samples will be dropped {}", serviceRoomId);
                        continue;
                    }
                }

                var callSamplesBuilder = CallSamples.builderFrom(callId);
                Iterator<ClientSamples> iterator = collectedClientSamples.streamByServiceRoomId(serviceRoomId).iterator();
                while(iterator.hasNext()) {
                    ClientSamples clientSamples = iterator.next();
                    callSamplesBuilder.withClientSamples(clientSamples);
                }
                CallSamples callSamples = callSamplesBuilder.build();
                collectedCallSamplesBuilder.withCallSamples(callSamples);
            }

            CollectedCallSamples result = collectedCallSamplesBuilder.build();
            this.observer.onNext(result);
        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public void onComplete() {

        }

        private UUID createCallIfNotExists(ServiceRoomId serviceRoomId, List<ClientSamples> clientSamplesList) {
            var task = createCallIfNotExistsTaskProvider.get();
            Optional<ClientSamples> firstClientSampleHolder = clientSamplesList.stream()
                    .filter(clientSamples -> Objects.nonNull(clientSamples.getMinTimestamp()))
                    .min((o1, o2) -> o1.getMinTimestamp().compareTo(o2.getMinTimestamp()));
            if (firstClientSampleHolder.isEmpty()) {
                return null;
            }
            ClientSamples firstClientSample = firstClientSampleHolder.get();
            Long startedTimestamp = firstClientSample.getTimestamp();
            task.withServiceRoomId(serviceRoomId)
                    .withStartedTimestamp(startedTimestamp)
                    .execute();

            if (!task.succeeded()) {
                return null;
            }
            var result = task.getResult();
            return result;
        }
    }
}
