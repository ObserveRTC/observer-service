package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ClientSideSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;


class ObservedClientSampleTest {

    final RandomGenerators generator = new RandomGenerators();

    final ClientSideSamplesGenerator samplesGenerator = new ClientSideSamplesGenerator();

    @Test
    void sanityTest() {
        var serviceId = generator.getRandomServiceId();
        var timeZoneId = generator.getRandomTimeZoneId();
        var mediaUnitId = generator.getRandomClientSideMediaUnitId();
        var clientSample = samplesGenerator.get().clientSamples[0];
        var observedClientSample = ObservedClientSample
                .builder()
                .setServiceId(serviceId)
                .setTimeZoneId(timeZoneId)
                .setMediaUnitId(mediaUnitId)
                .setClientSample(clientSample)
                .build();

        Assertions.assertEquals(serviceId, observedClientSample.getServiceId());
        Assertions.assertEquals(timeZoneId, observedClientSample.getTimeZoneId());
        Assertions.assertEquals(mediaUnitId, observedClientSample.getMediaUnitId());
        Assertions.assertNotNull(observedClientSample.getClientSample());
    }
}