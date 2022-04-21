package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.Objects;
import java.util.UUID;

@MicronautTest(environments = "test")
class CallEntitiesUpdaterTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallEntitiesUpdater callEntitiesUpdater;

    ObservedSamplesGenerator aliceObservedSamplesGenerator;
    ObservedSamplesGenerator bobObservedSamplesGenerator;

    @BeforeEach
    void setup() {
        this.aliceObservedSamplesGenerator = new ObservedSamplesGenerator();
        this.bobObservedSamplesGenerator = ObservedSamplesGenerator.createSharedRoomGenerator(this.aliceObservedSamplesGenerator);
        this.hazelcastMaps.getCalls().clear();
        this.hazelcastMaps.getClients().clear();
        this.hazelcastMaps.getPeerConnections().clear();
        this.hazelcastMaps.getMediaTracks().clear();
    }

    @Order(1)
    @Test
    void shouldAddEntities() {
        var observedClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder().addObservedClientSample(observedClientSample).build();
        this.callEntitiesUpdater.accept(observedClientSamples);

        var clientSample = observedClientSample.getClientSample();
        int numberOfMediaTracks = getLength(clientSample.inboundAudioTracks, clientSample.inboundVideoTracks, clientSample.outboundAudioTracks, clientSample.outboundVideoTracks);
        int numberOfPeerConnections = getLength(clientSample.pcTransports);
        Assertions.assertEquals(1, this.hazelcastMaps.getCalls().size(), "Number of created calls");
        Assertions.assertEquals(1, this.hazelcastMaps.getClients().size(), "The number of clients");
        Assertions.assertEquals(numberOfPeerConnections, this.hazelcastMaps.getPeerConnections().size(), "The number of peer connections");
        Assertions.assertEquals(numberOfMediaTracks, this.hazelcastMaps.getMediaTracks().size(), "The number of MediaTracks");
    }

    @Order(2)
    @Test
    void shouldDeletePreviousCallInSlaveMode() {
        var observedAliceSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var observedAliceSamples = ObservedClientSamples.builder().addObservedClientSample(observedAliceSample).build();
        this.callEntitiesUpdater.config.callIdAssignMode = ObserverConfig.EvaluatorsConfig.CallUpdater.CallIdAssignMode.SLAVE;
        this.callEntitiesUpdater.accept(observedAliceSamples);
        var expectedCallId = UUID.randomUUID();
        var observedBobSample = bobObservedSamplesGenerator.generateObservedClientSample(expectedCallId);
        var observedBobSamples = ObservedClientSamples.builder().addObservedClientSample(observedBobSample).build();
        this.callEntitiesUpdater.accept(observedBobSamples);
        var actualCallId = this.hazelcastMaps.getServiceRoomToCallIds().get(observedAliceSample.getServiceRoomId().getKey());
        Assertions.assertTrue(expectedCallId.equals(actualCallId), "assigned callId");
    }

    static<T> int getLength(T[]... arrays) {
        if (Objects.isNull(arrays)) return 0;
        var result = 0;
        for (var array : arrays) {
            if (Objects.isNull(array)) continue;
            result += array.length;
        }
        return result;
    }
}