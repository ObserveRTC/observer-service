package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.samples.ObservedClientSampleGenerator;

import javax.inject.Inject;

@MicronautTest
class PeerToPeerCallTest {

    @Inject
    ProcessingPipeline pipeline;

    @Inject
    ObservedClientSampleGenerator generator;

    @BeforeEach
    void setup() {

    }

    @Test
    public void getRoomServiceIds() {

    }

}