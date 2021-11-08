package org.observertc.webrtc.observer.samples;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

@MicronautTest
class ClientSamplesTest {

    @Inject
    ObservedClientSampleGenerator generator;
    private String serviceId;
    private String roomId;
    private UUID clientId;
    private String mediaUnitId;


    @BeforeEach
    void setup() {
        this.serviceId = UUID.randomUUID().toString();
        this.mediaUnitId = UUID.randomUUID().toString();
        this.roomId = UUID.randomUUID().toString();
        this.clientId = UUID.randomUUID();
        this.generator
                .withServiceId(this.serviceId)
                .withMediaUnitId(this.mediaUnitId)
                .withRoomId(this.roomId)
                .withClientId(this.clientId)
        ;
    }

    @Test
    public void iterableClientSamples() {
        var observedClientSample = this.generator.get();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(observedClientSample.getClientSample())
                .build();

        Assertions.assertNotNull(clientSamples.iterator());
    }

    @Test
    public void streamableClientSamples() {
        var observedClientSample = this.generator.get();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(observedClientSample.getClientSample())
                .build();

        Assertions.assertNotNull(clientSamples.stream());
    }

    @Test
    public void getClientId() {
        var observedClientSample = this.generator.get();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(observedClientSample.getClientSample())
                .build();

        Assertions.assertEquals(this.clientId, clientSamples.getClientId());
    }

    @Test
    public void getMediaUnitId() {
        var observedClientSample = this.generator.get();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(observedClientSample.getClientSample())
                .build();

        Assertions.assertEquals(this.mediaUnitId, clientSamples.getMediaUnitId());
    }

    @Test
    public void getServiceId() {
        var observedClientSample = this.generator.get();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(observedClientSample.getClientSample())
                .build();

        Assertions.assertEquals(this.serviceId, clientSamples.getServiceId());
    }

    @Test
    public void getMinTimestamp() {
        var observedClientSample = this.generator.get();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(observedClientSample.getClientSample())
                .build();

        Assertions.assertEquals(observedClientSample.getTimestamp(), clientSamples.getMinTimestamp());
    }

    @Test
    public void getTimestamp() {
        var observedClientSample = this.generator.get();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(observedClientSample.getClientSample())
                .build();

        Assertions.assertEquals(observedClientSample.getTimestamp(), clientSamples.getTimestamp());
    }

    @Test
    public void getInboundAudioTrackIds() {
        var observedClientSample = this.generator.get();
        var clientSample = observedClientSample.getClientSample();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(clientSample)
                .build();
        var trackIds = clientSamples.getInboundMediaTrackIds();

        ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                .map(t -> UUID.fromString(t.trackId))
                .map(trackIds::contains)
                .forEach(Assertions::assertTrue);
    }

    @Test
    public void getInboundVideoTrackIds() {
        var observedClientSample = this.generator.get();
        var clientSample = observedClientSample.getClientSample();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(clientSample)
                .build();
        var trackIds = clientSamples.getInboundMediaTrackIds();

        ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                .map(t -> UUID.fromString(t.trackId))
                .map(trackIds::contains)
                .forEach(Assertions::assertTrue);
    }

    @Test
    public void getOutboundAudioTrackIds() {
        var observedClientSample = this.generator.get();
        var clientSample = observedClientSample.getClientSample();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(clientSample)
                .build();
        var trackIds = clientSamples.getOutboundMediaTrackIds();

        ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
                .map(t -> UUID.fromString(t.trackId))
                .map(trackIds::contains)
                .forEach(Assertions::assertTrue);
    }

    @Test
    public void getOutboundVideoTrackIds() {
        var observedClientSample = this.generator.get();
        var clientSample = observedClientSample.getClientSample();

        var clientSamples = ClientSamples
                .builderFrom(observedClientSample)
                .withClientSample(clientSample)
                .build();
        var trackIds = clientSamples.getOutboundMediaTrackIds();

        ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
                .map(t -> UUID.fromString(t.trackId))
                .map(trackIds::contains)
                .forEach(Assertions::assertTrue);
    }

}