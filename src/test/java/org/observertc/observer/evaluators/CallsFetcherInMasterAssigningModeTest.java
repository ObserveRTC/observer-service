package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.CallsRepository;
import org.observertc.observer.repositories.RoomsRepository;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.UUID;

@MicronautTest
class CallsFetcherInMasterAssigningModeTest {

    @Inject
    RoomsRepository roomsRepository;

    @Inject
    CallsFetcherInMasterAssigningMode observedCallsFetcher;

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
    void shouldAddCall() {
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .build();

        var callsFetcherResult = this.observedCallsFetcher.fetchFor(observedClientSamples);
        var aliceRoom = this.roomsRepository.get(aliceClientSample.getServiceRoomId());
        Assertions.assertNotNull(aliceRoom);
        Assertions.assertEquals(1, callsFetcherResult.actualCalls().size());

        var aliceCall = callsFetcherResult.actualCalls().get(aliceRoom.getServiceRoomId());
        Assertions.assertNotNull(aliceCall);
    }

    @Test
    void shouldNotAddCallTwice() {
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var bobClientSample = bobObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .add(bobClientSample.getServiceId(), bobClientSample.getMediaUnitId(), bobClientSample.getClientSample())
                .build();

        var call_1 = this.observedCallsFetcher.fetchFor(observedClientSamples).actualCalls().get(aliceClientSample.getServiceRoomId());
        var call_2 = this.observedCallsFetcher.fetchFor(observedClientSamples).actualCalls().get(bobClientSample.getServiceRoomId());
        Assertions.assertEquals(call_1.getCallId(), call_2.getCallId());
    }

    @Test
    void shouldHaveCall() {
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var callsFetcherResult_1 = this.observedCallsFetcher.fetchFor(ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .build()
        );

        var bobClientSample = bobObservedSamplesGenerator.generateObservedClientSample();
        var callsFetcherResult_2 = this.observedCallsFetcher.fetchFor(ObservedClientSamples.builder()
                .add(bobClientSample.getServiceId(), bobClientSample.getMediaUnitId(), bobClientSample.getClientSample())
                .build()
        );

        Assertions.assertEquals(callsFetcherResult_1.actualCalls().size(), 1);
        Assertions.assertEquals(callsFetcherResult_2.actualCalls().size(), 1);
    }

    @Test
    void shouldHaveCall_2() {
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        aliceClientSample.getClientSample().callId = UUID.randomUUID().toString();
        var callsFetcherResult_1 = this.observedCallsFetcher.fetchFor(ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .build()
        );

        var bobClientSample = bobObservedSamplesGenerator.generateObservedClientSample();
        bobClientSample.getClientSample().callId = UUID.randomUUID().toString();
        var callsFetcherResult_2 = this.observedCallsFetcher.fetchFor(ObservedClientSamples.builder()
                .add(bobClientSample.getServiceId(), bobClientSample.getMediaUnitId(), bobClientSample.getClientSample())
                .build()
        );

        Assertions.assertEquals(callsFetcherResult_1.actualCalls().size(), 1);
        Assertions.assertEquals(callsFetcherResult_2.actualCalls().size(), 1);
    }
}