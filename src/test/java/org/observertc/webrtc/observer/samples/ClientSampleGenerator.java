package org.observertc.webrtc.observer.samples;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

@Prototype
public class ClientSampleGenerator {

    private EasyRandom generator;

    public ClientSampleGenerator() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        this.generator = new EasyRandom(parameters);
    }

    public ClientSample generate() {
        return this.generator.nextObject(ClientSample.class);
    }
}
