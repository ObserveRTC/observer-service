package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ObservedSamplesGenerator;

@MicronautTest(environments = "test")
class CallEntitiesUpdaterInSlaveModeTest {


    @Inject
    CallEntitiesUpdater callEntitiesUpdater;

    ObservedSamplesGenerator aliceObservedSamplesGenerator;
    ObservedSamplesGenerator bobObservedSamplesGenerator;

    @BeforeEach
    void setup() {
        this.aliceObservedSamplesGenerator = new ObservedSamplesGenerator();
        this.bobObservedSamplesGenerator = ObservedSamplesGenerator.createSharedRoomGenerator(this.aliceObservedSamplesGenerator);
    }

    @Test
    void shouldDeletePreviousCall() {
//        var observedAliceSample = aliceObservedSamplesGenerator.generateObservedClientSample();
//        var observedAliceSamples = ObservedClientSamples.builder().addObservedClientSample(observedAliceSample).build();
//        this.callEntitiesUpdater.config.callIdAssignMode = ObserverConfig.EvaluatorsConfig.CallUpdater.CallIdAssignMode.SLAVE;
//        this.callEntitiesUpdater.accept(observedAliceSamples);
//        var expectedCallId = UUID.randomUUID();
//        var observedBobSample = bobObservedSamplesGenerator.generateObservedClientSample(expectedCallId);
//        var observedBobSamples = ObservedClientSamples.builder().addObservedClientSample(observedBobSample).build();
//        this.callEntitiesUpdater.accept(observedBobSamples);
    }
}