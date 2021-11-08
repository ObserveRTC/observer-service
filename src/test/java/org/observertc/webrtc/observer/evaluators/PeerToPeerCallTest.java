package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.common.Sleeper;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@MicronautTest
class PeerToPeerCallTest {

    private final String serviceId = "myService";
    private final String mediaUnitId = "myApp";
    private final String roomId = "myTest";

    @Inject
    ClientSurrogate alice;

    @Inject
    ClientSurrogate bob;

    @Inject
    TestCallProcessor testCallProcessor;


    @BeforeEach
    void setup() {
        this.alice
                .withUserId("Alice")
                .withClientId(UUID.randomUUID());

        this.bob
                .withUserId("Bob")
                .withClientId(UUID.randomUUID());

        this.testCallProcessor
                .withClientSurrogate(this.alice)
                .withClientSurrogate(this.bob)
                .withServiceId(this.serviceId)
                .withMediaUnitId(this.mediaUnitId)
                .withRoomId(this.roomId);

    }

    @Test
    public void checkReports() throws ExecutionException, InterruptedException, TimeoutException {
        this.testCallProcessor.start();
        new Sleeper(() -> 200000).run();
        return;
    }
}