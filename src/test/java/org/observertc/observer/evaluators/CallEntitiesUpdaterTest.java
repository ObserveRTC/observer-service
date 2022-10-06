package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.time.Instant;
import java.util.Objects;

@MicronautTest(environments = "test")
class CallEntitiesUpdaterTest {

    @Inject
    CallEntitiesUpdater callEntitiesUpdater;

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;
    //
    @Inject
    InboundTracksRepository inboundTracksRepository;

    @Inject
    OutboundTracksRepository outboundTracksRepository;

    ObservedSamplesGenerator aliceObservedSamplesGenerator;
    ObservedSamplesGenerator bobObservedSamplesGenerator;

    @BeforeEach
    void setup() {
        this.aliceObservedSamplesGenerator = new ObservedSamplesGenerator();
        this.bobObservedSamplesGenerator = ObservedSamplesGenerator.createSharedRoomGenerator(this.aliceObservedSamplesGenerator);
    }

    @Test
    void shouldAddCalls() {
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var bobClientSample = bobObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder()
                .addObservedClientSample(aliceClientSample)
                .addObservedClientSample(bobClientSample)
                .build();

        this.callEntitiesUpdater.accept(observedClientSamples);
        var calls = this.callsRepository.getAll(observedClientSamples.getServiceRoomIds());
        Assertions.assertEquals(1, calls.size());

        var aliceCall = calls.get(aliceClientSample.getServiceRoomId());
        Assertions.assertNotNull(aliceCall);

        var bobCall = calls.get(bobClientSample.getServiceRoomId());
        Assertions.assertNotNull(bobCall);

        Assertions.assertEquals(aliceCall, bobCall);

    }

    @Test
    void shouldAddClients() {
        var observedClientSamples = this.generateObservedClientSamples();

        this.callEntitiesUpdater.accept(observedClientSamples);

        var clientIds = observedClientSamples.getClientIds();
        var clients = this.clientsRepository.getAll(clientIds);

        Assertions.assertEquals(clients.size(), clientIds.size());
    }

    @Test
    void shouldAddPeerConnections() {
        var observedClientSamples = this.generateObservedClientSamples();

        this.callEntitiesUpdater.accept(observedClientSamples);

        var peerConnectionIds = observedClientSamples.getPeerConnectionIds();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionIds);

        Assertions.assertEquals(peerConnections.size(), peerConnectionIds.size());
    }

    @Test
    void shouldAddInboundTracks() {
        var observedClientSamples = this.generateObservedClientSamples();

        this.callEntitiesUpdater.accept(observedClientSamples);

        var inboundTrackIds = observedClientSamples.getInboundTrackIds();
        var inboundTracks = this.inboundTracksRepository.getAll(inboundTrackIds);

        Assertions.assertEquals(inboundTrackIds.size(), inboundTracks.size());
    }

    @Test
    void shouldAddOutboundTracks() {
        var observedClientSamples = this.generateObservedClientSamples();

        this.callEntitiesUpdater.accept(observedClientSamples);

        var outboundTrackIds = observedClientSamples.getOutboundTrackIds();
        var outboundTracks = this.outboundTracksRepository.getAll(outboundTrackIds);

        Assertions.assertEquals(outboundTrackIds.size(), outboundTracks.size());
    }

    @Test
    void shouldTouchClients() {
        var observedClientSamples = this.generateObservedClientSamples();
        this.callEntitiesUpdater.accept(observedClientSamples);
        var expectedTouch = this.updateObservedClientSamplesTimestamp(observedClientSamples);
        this.callEntitiesUpdater.accept(observedClientSamples);

        var clientIds = observedClientSamples.getClientIds();
        var clients = this.clientsRepository.getAll(clientIds);

        Assertions.assertEquals(clients.size(), clientIds.size());
        for (var client : clients.values()) {
            Assertions.assertEquals(expectedTouch, client.getTouched());
        }
    }

    @Test
    void shouldTouchPeerConnections() {
        var observedClientSamples = this.generateObservedClientSamples();
        this.callEntitiesUpdater.accept(observedClientSamples);
        var expectedTouch = this.updateObservedClientSamplesTimestamp(observedClientSamples);
        this.callEntitiesUpdater.accept(observedClientSamples);

        this.callEntitiesUpdater.accept(observedClientSamples);

        var peerConnectionIds = observedClientSamples.getPeerConnectionIds();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionIds);

        Assertions.assertEquals(peerConnections.size(), peerConnectionIds.size());
        for (var peerConnection : peerConnections.values()) {
            Assertions.assertEquals(expectedTouch, peerConnection.getTouched());
        }
    }

    @Test
    void shouldTouchInboundTracks() {
        var observedClientSamples = this.generateObservedClientSamples();
        this.callEntitiesUpdater.accept(observedClientSamples);
        var expectedTouch = this.updateObservedClientSamplesTimestamp(observedClientSamples);
        this.callEntitiesUpdater.accept(observedClientSamples);

        this.callEntitiesUpdater.accept(observedClientSamples);

        var inboundTrackIds = observedClientSamples.getInboundTrackIds();
        var inboundTracks = this.inboundTracksRepository.getAll(inboundTrackIds);

        Assertions.assertEquals(inboundTrackIds.size(), inboundTracks.size());
        for (var inboundTrack : inboundTracks.values()) {
            Assertions.assertEquals(expectedTouch, inboundTrack.getTouched());
        }
    }

    @Test
    void shouldTouchOutboundTracks() {
        var observedClientSamples = this.generateObservedClientSamples();
        this.callEntitiesUpdater.accept(observedClientSamples);
        var expectedTouch = this.updateObservedClientSamplesTimestamp(observedClientSamples);
        this.callEntitiesUpdater.accept(observedClientSamples);

        this.callEntitiesUpdater.accept(observedClientSamples);

        var outboundTrackIds = observedClientSamples.getOutboundTrackIds();
        var outboundTracks = this.outboundTracksRepository.getAll(outboundTrackIds);

        Assertions.assertEquals(outboundTrackIds.size(), outboundTracks.size());
        for (var outboundTrack : outboundTracks.values()) {
            Assertions.assertEquals(expectedTouch, outboundTrack.getTouched());
        }
    }


    private ObservedClientSamples generateObservedClientSamples() {
        var aliceClientSample = aliceObservedSamplesGenerator.generateObservedClientSample();
        var bobClientSample = bobObservedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder()
                .addObservedClientSample(aliceClientSample)
                .addObservedClientSample(bobClientSample)
                .build();
        return observedClientSamples;
    }

    private Long updateObservedClientSamplesTimestamp(ObservedClientSamples observedClientSamples) {
        var timestamp = Instant.now().toEpochMilli();
        for (var observedClientSample : observedClientSamples) {
            observedClientSample.getClientSample().timestamp = timestamp;
        }
        return timestamp;
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