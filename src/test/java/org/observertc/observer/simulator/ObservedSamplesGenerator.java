package org.observertc.observer.simulator;

import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.observer.utils.RandomGenerators;

import java.util.UUID;

public class ObservedSamplesGenerator {

    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final String SERVICE_ID = randomGenerators.getRandomServiceId();
    private final String CLIENT_MEDIA_UNIT_ID = randomGenerators.getRandomClientSideMediaUnitId();
    private final String TIME_ZONE_ID = randomGenerators.getRandomTimeZoneId();
    private Networks.ClientsToSfuConnection network = Networks.createClientsToSfuConnection();

    public ObservedClientSample generateObservedClientSample() {
        return generateObservedClientSample(null);
    }

    public ObservedClientSample generateObservedClientSample(UUID callId) {
        var samples = this.clientSurrogate.generateSamples();
        var clientSample = samples.clientSamples[0];
        clientSample.callId = callId;
        var result = ObservedClientSample.builder()
                .setClientSample(clientSample)
                .setTimeZoneId(TIME_ZONE_ID)
                .setServiceId(SERVICE_ID)
                .setMediaUnitId(CLIENT_MEDIA_UNIT_ID)
                .build();
        return result;
    }

    public ObservedSfuSample generateObservedSfuSample() {

    }

}
