package org.observertc.webrtc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.repositories.tasks.CreateCallIfNotExistsTask;
import org.observertc.webrtc.observer.repositories.tasks.FindCallIdsByServiceRoomIds;
import org.observertc.webrtc.observer.samples.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

/**
 * Responsible to map the received client sample into an organized map of ClientSamples,
 * so the successor components can work by a batch of samples groupped by clients
 */
@Prototype
public class CollectCallSamples implements Function<List<ObservedClientSample>, Optional<CollectedCallSamples>> {

    private static final Logger logger = LoggerFactory.getLogger(CollectCallSamples.class);

    @Inject
    Provider<CreateCallIfNotExistsTask> createCallIfNotExistsTaskProvider;

    @Inject
    Provider<FindCallIdsByServiceRoomIds> findCallsTaskProvider;


    @PostConstruct
    void setup() {

    }


    @Override
    @Timed(value = "observertc-evaluators-collect-call-samples")
    public Optional<CollectedCallSamples> apply(List<ObservedClientSample> observedClientSamples) throws Throwable {
        if (Objects.isNull(observedClientSamples) || observedClientSamples.size() < 1) {
            return Optional.empty();
        }
        Map<ServiceRoomId, RoomSamples.Builder> roomSampleBuilders = new HashMap<>();
        for (ObservedClientSample observedClientSample : observedClientSamples) {
            ServiceRoomId serviceRoomId = ServiceRoomId.make(
                    observedClientSample.getServiceId(),
                    observedClientSample.getRoomId()
            );
            RoomSamples.Builder roomSampleBuilder = roomSampleBuilders.get(serviceRoomId);
            if (Objects.isNull(roomSampleBuilder)) {
                roomSampleBuilder = RoomSamples.builderFrom(serviceRoomId);
                roomSampleBuilders.put(serviceRoomId, roomSampleBuilder);
            }
            roomSampleBuilder.withObservedClientSample(observedClientSample);
        }

        Map<ServiceRoomId, UUID> foundCallIds = this.findCallIds(roomSampleBuilders.keySet());
        CollectedCallSamples.Builder collectedCallSamples = CollectedCallSamples.builder();
        roomSampleBuilders.forEach(((serviceRoomId, builder) -> {
            var roomSamples = builder.build();
            UUID callId = foundCallIds.get(serviceRoomId);
            if (Objects.isNull(callId)) {
                callId = this.createCallIfNotExists(roomSamples);
                if (Objects.isNull(callId)) {
                    logger.warn("Cannot create call for serviceRoom {}. Samples related to this room are dropped", serviceRoomId);
                    return;
                }
            }

            CallSamplesBuilder callSamplesBuilder = CallSamples
                    .builderFrom(callId, serviceRoomId);

            roomSamples
                    .stream()
                    .forEach(callSamplesBuilder::withClientSamples);

            var callSamples = callSamplesBuilder.build();
            collectedCallSamples.withCallSamples(callSamples);
        }));

        return Optional.of(
                collectedCallSamples.build()
        );
    }

    private Map<ServiceRoomId, UUID> findCallIds(Set<ServiceRoomId> serviceRoomIds) {
        var findCallsTask = findCallsTaskProvider.get()
                .whereServiceRoomIds(serviceRoomIds)
                .withUnmodifiableResult(false);

        if (!findCallsTask.execute().succeeded()) {
            logger.warn("Finding CallIds to rooms have failed. In case new call ids are created, then inconsistency might happen");
            return Collections.EMPTY_MAP;
        }

        return findCallsTask.getResult();
    }

    private UUID createCallIfNotExists(RoomSamples roomSamples) {
        var task = createCallIfNotExistsTaskProvider.get();
        task.withServiceRoomId(roomSamples.getServiceRoomId())
                .withStartedTimestamp(roomSamples.getMinTimestamp())
                .execute();

        if (!task.succeeded()) {
            return null;
        }
        var result = task.getResult();
        return result;
    }
}
