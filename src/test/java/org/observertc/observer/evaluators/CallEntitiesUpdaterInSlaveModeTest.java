package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.CallsRepository;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.UUID;

@MicronautTest(environments = "test")
class CallEntitiesUpdaterInSlaveModeTest {


    @Inject
    CallEntitiesUpdater callEntitiesUpdater;

    @Inject
    CallsRepository callsRepository;

    ObservedSamplesGenerator aliceObservedSamplesGenerator;
    ObservedSamplesGenerator bobObservedSamplesGenerator;

    @BeforeEach
    void setup() {
        this.aliceObservedSamplesGenerator = new ObservedSamplesGenerator();
        this.bobObservedSamplesGenerator = ObservedSamplesGenerator.createSharedRoomGenerator(this.aliceObservedSamplesGenerator);
    }

    @Test
    void shouldDeletePreviousCall() {
        this.callEntitiesUpdater.config.callIdAssignMode = ObserverConfig.EvaluatorsConfig.CallUpdater.CallIdAssignMode.SLAVE;

        var expectedOldCallId = UUID.randomUUID().toString();
        var observedAliceSample = aliceObservedSamplesGenerator.generateObservedClientSample(expectedOldCallId);
        var observedAliceSamples = ObservedClientSamples.builder().add(
                        observedAliceSample.getServiceId(), observedAliceSample.getMediaUnitId(), observedAliceSample.getClientSample())
                .build();
        this.callEntitiesUpdater.accept(observedAliceSamples);
        var aliceCall = this.callsRepository.get(expectedOldCallId);

        Assertions.assertEquals(expectedOldCallId, aliceCall.getCallId());

        Assertions.assertEquals(1, aliceCall.getClientIds().size());
        Assertions.assertNotNull(aliceCall.getClient(observedAliceSample.getClientSample().clientId));


        var expectedNewCallId = UUID.randomUUID().toString();
        var observedBobSample = bobObservedSamplesGenerator.generateObservedClientSample(expectedNewCallId);
        var observedBobSamples = ObservedClientSamples.builder().
            add(observedBobSample.getServiceId(), observedBobSample.getMediaUnitId(), observedBobSample.getClientSample())
                .build();
        this.callEntitiesUpdater.accept(observedBobSamples);
        var bobCall = this.callsRepository.get(expectedNewCallId);

        Assertions.assertEquals(expectedNewCallId, bobCall.getCallId());

        Assertions.assertEquals(1, bobCall.getClientIds().size());
        Assertions.assertNotNull(bobCall.getClient(observedBobSample.getClientSample().clientId));
    }
}