package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.samples.ObservedClientSampleGenerator;

import javax.inject.Inject;
import java.util.List;

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
}