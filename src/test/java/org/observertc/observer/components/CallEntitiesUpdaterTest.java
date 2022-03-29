package org.observertc.observer.components;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import javax.inject.Inject;
import java.util.Objects;

@MicronautTest
class CallEntitiesUpdaterTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallEntitiesUpdater callEntitiesUpdater;

    ObservedSamplesGenerator observedSamplesGenerator = new ObservedSamplesGenerator();

    @Test
    void shouldAddEntities() {
        var observedClientSample = observedSamplesGenerator.generateObservedClientSample();
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