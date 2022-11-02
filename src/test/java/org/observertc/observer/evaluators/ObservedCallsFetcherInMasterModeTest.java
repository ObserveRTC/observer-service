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

@MicronautTest
class ObservedCallsFetcherInMasterModeTest {

    @Inject
    RoomsRepository roomsRepository;

    @Inject
    ObservedCallsFetcherInMasterMode observedCallsFetcher;

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

        var calls = this.observedCallsFetcher.fetchFor(observedClientSamples);
        var aliceRoom = this.roomsRepository.get(aliceClientSample.getServiceRoomId());
        Assertions.assertNotNull(aliceRoom);
        Assertions.assertEquals(1, calls.size());

        var aliceCall = calls.get(aliceRoom.getServiceRoomId());
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

        var call_1 = this.observedCallsFetcher.fetchFor(observedClientSamples).get(aliceClientSample.getServiceRoomId());
        var call_2 = this.observedCallsFetcher.fetchFor(observedClientSamples).get(bobClientSample.getServiceRoomId());
        Assertions.assertEquals(call_1.getCallId(), call_2.getCallId());
    }
}