package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.RandomGenerators;
import org.observertc.observer.utils.SfuSideSamplesGenerator;

class ObservedSfuSampleTest {

    final RandomGenerators generator = new RandomGenerators();

    final SfuSideSamplesGenerator samplesGenerator = new SfuSideSamplesGenerator();

    @Test
    void sanityTest() {
        var serviceId = generator.getRandomServiceId();
        var timeZoneId = generator.getRandomTimeZoneId();
        var mediaUnitId = generator.getRandomClientSideMediaUnitId();
        var sfuSample = samplesGenerator.get().sfuSamples[0];
        var observedClientSample = ObservedSfuSample
                .builder()
                .setServiceId(serviceId)
                .setTimeZoneId(timeZoneId)
                .setMediaUnitId(mediaUnitId)
                .setSfuSample(sfuSample)
                .build();

        Assertions.assertEquals(serviceId, observedClientSample.getServiceId());
        Assertions.assertEquals(timeZoneId, observedClientSample.getTimeZoneId());
        Assertions.assertEquals(mediaUnitId, observedClientSample.getMediaUnitId());
        Assertions.assertNotNull(observedClientSample.getSfuSample());
    }
}