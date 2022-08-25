package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.observer.samples.ObservedSfuSamples;

import java.util.Objects;

@MicronautTest
class SfuEntitiesUpdaterTest {
    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    SfuEntitiesUpdater sfuEntitiesUpdater;

    ObservedSamplesGenerator observedSamplesGenerator = new ObservedSamplesGenerator();

    @Test
    void shouldAddEntities() {
        var observedSfuSample = observedSamplesGenerator.generateObservedSfuSample();
        var observedSfuSamples = ObservedSfuSamples.builder().addObservedSfuSample(observedSfuSample).build();
        this.sfuEntitiesUpdater.accept(observedSfuSamples);

        var sfuSample = observedSfuSample.getSfuSample();
        int numberOfRtpPads = getLength(sfuSample.inboundRtpPads, sfuSample.outboundRtpPads);
        int numberOfTransports = getLength(sfuSample.transports);
        Assertions.assertEquals(1, this.hazelcastMaps.getSFUs().size(), "The number of SFUs");
        Assertions.assertEquals(numberOfRtpPads, this.hazelcastMaps.getSFURtpPads().size(), "The number of Sfu Rtp Pads");
        Assertions.assertEquals(numberOfTransports, this.hazelcastMaps.getSFUTransports().size(), "The number of Sfu Transports");
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