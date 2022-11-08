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
class CallsFetcherInSlaveAssigningModeTest {

    @Inject
    RoomsRepository roomsRepository;

    @Inject
    CallsFetcherInSlaveAssigningMode observedCallsFetcher;

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
    void shouldNotAddCallIfCallIdIsNotProvided() {
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .build();

        var callsFetcherResult = this.observedCallsFetcher.fetchFor(observedClientSamples);
        var aliceRoom = this.roomsRepository.get(aliceClientSample.getServiceRoomId());
        Assertions.assertNull(aliceRoom);
    }

    @Test
    void shouldAddCallIfCallIdIsProvided() {
        var callId = UUID.randomUUID().toString();
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .build();
        aliceClientSample.getClientSample().callId = callId;

        var callsFetcherResult = this.observedCallsFetcher.fetchFor(observedClientSamples);
        Assertions.assertEquals(1, callsFetcherResult.actualCalls().size());
        Assertions.assertEquals(0, callsFetcherResult.remedyClients().size());

        var aliceRoom = this.roomsRepository.get(aliceClientSample.getServiceRoomId());
        var aliceCall = callsFetcherResult.actualCalls().get(aliceRoom.getServiceRoomId());
        Assertions.assertNotNull(aliceRoom);
        Assertions.assertEquals(aliceCall.getCallId(), callId);
    }


    @Test
    void shouldAssignNewCallAndMakeRemedyClient() {
        var oldCallId = UUID.randomUUID().toString();
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples_1 = ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .build();
        aliceClientSample.getClientSample().callId = oldCallId;
        this.observedCallsFetcher.fetchFor(observedClientSamples_1);
        this.callsRepository.get(oldCallId).addClient(
                aliceClientSample.getClientSample().clientId,
                aliceClientSample.getClientSample().userId,
                aliceClientSample.getMediaUnitId(),
                aliceClientSample.getTimeZoneId(),
                aliceClientSample.getClientSample().timestamp,
                aliceClientSample.getClientSample().marker
        );

        var newCallId = UUID.randomUUID().toString();
        var bobClientSample = bobObservedSamplesGenerator.generateObservedClientSample();
        bobClientSample.getClientSample().callId = newCallId;

        var observedClientSamples_2 = ObservedClientSamples.builder()
                .add(aliceClientSample.getServiceId(), aliceClientSample.getMediaUnitId(), aliceClientSample.getClientSample())
                .add(bobClientSample.getServiceId(), bobClientSample.getMediaUnitId(), bobClientSample.getClientSample())
                .build();
        var callFetchResult = this.observedCallsFetcher.fetchFor(observedClientSamples_2);
        Assertions.assertEquals(1, callFetchResult.actualCalls().size());
        Assertions.assertEquals(1, callFetchResult.remedyClients().size());

        var aliceCall = this.callsRepository.get(oldCallId);
        var bobCall = this.callsRepository.get(newCallId);
        var room = this.roomsRepository.get(aliceClientSample.getServiceRoomId());
        Assertions.assertNotNull(aliceCall);
        Assertions.assertNotNull(bobCall);
        Assertions.assertNotNull(room);

        Assertions.assertEquals(room.getCallId(), newCallId);
        Assertions.assertEquals(aliceCall.getCallId(), oldCallId);
        Assertions.assertEquals(bobCall.getCallId(), newCallId);
    }

}