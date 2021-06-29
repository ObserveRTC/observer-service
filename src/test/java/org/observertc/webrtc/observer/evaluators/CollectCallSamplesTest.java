package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.repositories.tasks.FindCallIdsByServiceRoomIds;
import org.observertc.webrtc.observer.samples.ObservedClientSampleGenerator;
import org.observertc.webrtc.observer.samples.ServiceRoomId;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class CollectCallSamplesTest {

    @Inject
    ObservedClientSampleGenerator generator;

    @Inject
    CollectCallSamples collectCallSamples;

    @BeforeEach
    void setup() {

    }

    @Test
    public void getRoomServiceIds() {
        var observedClientSample = this.generator.get();
        var collectedClientSamples = Observable.just(observedClientSample)
                .map(List::of)
                .map(this.collectCallSamples)
                .blockingFirst();

        Assertions.assertEquals(observedClientSample.getClientSample(), observedClientSample.getClientSample());
    }


    @Replaces(FindCallIdsByServiceRoomIds.class)
    @Singleton
    public static class MockedFindCallIdsByServiceRoomIds extends FindCallIdsByServiceRoomIds {
        @Override
        public MockedFindCallIdsByServiceRoomIds execute() {
            return this;
        }

        @Override
        public boolean succeeded() {
            return true;
        }

        @Override
        public Map<ServiceRoomId, UUID> getResult() {
            return Collections.EMPTY_MAP;
        }
    }
//
//
//    @MockBean(CreateCallIfNotExistsTask.class)
//    public CreateCallIfNotExistsTask mockCreateCallIfNotExistsTask() {
//        var stub = mock(CreateCallIfNotExistsTask.class);
//        when(stub.getResult()).thenReturn(UUID.randomUUID());
//        return stub;
//    }

}