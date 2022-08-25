package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.Objects;

@MicronautTest(environments = "test")
class CallEntitiesUpdaterTest {

    @Inject
    HamokStorages hazelcastMaps;

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