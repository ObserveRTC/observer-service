package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.repositories.tasks.CreateCallIfNotExistsTask;
import org.observertc.webrtc.observer.repositories.tasks.FindCallIdsByServiceRoomIds;
import org.observertc.webrtc.observer.samples.ObservedSampleBuilder;
import org.observertc.webrtc.observer.samples.ObservedSampleBuilders;
import org.observertc.webrtc.observer.samples.ServiceRoomId;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Responsible to assign callId to observed sample builders
 *
 * TODO: should be an operator and should be renamed to AssignCallOperator
 */
@Prototype
public class CallAssigner implements Function<ObservedSampleBuilders, ObservedSampleBuilders> {
    private static final Logger logger = LoggerFactory.getLogger(CallAssigner.class);

    private Subject<CallEventReport> callEventSubject = PublishSubject.create();

    @Inject
    CallEventsBuilders callEventsBuilders;

    @Inject
    Provider<CreateCallIfNotExistsTask> createCallIfNotExistsTaskProvider;

    @Inject
    Provider<FindCallIdsByServiceRoomIds> findCallsTaskProvider;

    @PostConstruct
    void setup() {

    }

    public Observable<CallEventReport> getObservableCallEvent() {
        return this.callEventSubject;
    }

    @Override
    public ObservedSampleBuilders apply(ObservedSampleBuilders builders) throws Throwable {
        var serviceRoomIds = builders.getServiceRoomIds();
        var findCallIdsTask = findCallsTaskProvider.get()
                .whereServiceRoomIds(serviceRoomIds)
                .withUnmodifiableResult(false);

        if (!findCallIdsTask.execute().succeeded()) {
            logger.warn("Cannot execute {} all sample are dropped from the buffer", findCallIdsTask.getClass().getSimpleName());
            return null;
        }
        Map<ServiceRoomId, UUID> serviceRoomIdToCallIds = findCallIdsTask.getResult();
        Iterator<Map.Entry<ServiceRoomId, ObservedSampleBuilder>> it = builders.iterateByServiceRoomIds();
        while(it.hasNext()) {
            var entry = it.next();
            ServiceRoomId serviceRoomId = entry.getKey();
            ObservedSampleBuilder builder = entry.getValue();
            UUID callId = serviceRoomIdToCallIds.get(serviceRoomId);
            if (Objects.nonNull(callId)) {
                builder.withCallId(callId);
                continue;
            }
            callId = this.createCallIfNotExists(serviceRoomId, builder);
            if (Objects.isNull(callId)) {
                logger.warn("Cannot assign call to a sample. Sample will be dropped");
                continue;
            }
            serviceRoomIdToCallIds.put(serviceRoomId, callId);
            builder.withCallId(callId);
            var callEvent = this.callEventsBuilders.makeStartedCallEventReportBuilder()
                    .setServiceId(builder.getServiceId())
                    .setMediaUnitId(builder.getMediaUnitId())
                    .setMarker(builder.getMarker())
                    .setTimestamp(builder.getTimestamp())
                    .setCallId(callId.toString())
                    .setRoomId(builder.getRoomId())
                    .setUserId(builder.getUserId())
                    .setPeerConnectionId(null) // it does not matter
                    .setSampleTimestamp(builder.getTimestamp())
                    .setMessage("Call is started")
                    .setValue(null)
                    .setAttachments(null)
                    .build();
            this.callEventSubject.onNext(callEvent);
        }
        return builders;
    }


    private UUID createCallIfNotExists(ServiceRoomId serviceRoomId, ObservedSampleBuilder sampleBuilder) {
        var task = this.createCallIfNotExistsTaskProvider.get();
        task.withServiceRoomId(serviceRoomId)
                .withStartedTimestamp(sampleBuilder.getTimestamp())
                .execute();

        if (!task.succeeded()) {
            return null;
        }
        var result = task.getResult();
        return result;
    }
}
