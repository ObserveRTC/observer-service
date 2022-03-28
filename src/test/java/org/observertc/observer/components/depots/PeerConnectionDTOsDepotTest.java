package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PeerConnectionDTOsDepotTest {
    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private PeerConnectionDTOsDepot depot = new PeerConnectionDTOsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var clientSample = observedClientSample.getClientSample();
        var pcTransport = clientSample.pcTransports[0];
        this.depot
                .setObservedClientSample(observedClientSample)
                .setPeerConnectionTransport(pcTransport)
                .assemble();
        ;

        var actual = depot.get().get(pcTransport.peerConnectionId);

        Assertions.assertEquals(clientSample.callId, actual.callId, "callId field");
        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(clientSample.roomId, actual.roomId, "roomId field");

        Assertions.assertEquals(clientSample.clientId, actual.clientId, "clientId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(clientSample.userId, actual.userId, "mediaUnitId field");

        Assertions.assertEquals(pcTransport.peerConnectionId, actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(clientSample.timestamp, actual.created, "created field");

    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }


    @Test
    @Order(3)
    void shouldNotCreate_1() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var clientSample = observedClientSample.getClientSample();
        var pcTransport = clientSample.pcTransports[0];
        this.depot
//                .setObservedClientSample(observedClientSample)
                .setPeerConnectionTransport(pcTransport)
                .assemble()
        ;

        Assertions.assertEquals(0, depot.get().size());
    }

    @Test
    @Order(4)
    void shouldNotCreate_2() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var clientSample = observedClientSample.getClientSample();
        this.depot
                .setObservedClientSample(observedClientSample)
//                .setPeerConnectionTransport(pcTransport)
                .assemble()
        ;

        Assertions.assertEquals(0, depot.get().size());
    }
}