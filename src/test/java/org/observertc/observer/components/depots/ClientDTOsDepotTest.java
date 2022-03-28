package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientDTOsDepotTest {

    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private final ClientDTOsDepot depot = new ClientDTOsDepot();

    @Test
    @Order(1)
    void shouldMakeDTO() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        depot.addFromObservedClientSample(observedClientSample);
        var clientSample = observedClientSample.getClientSample();
        var clientDTO = depot.get().get(clientSample.clientId);

        Assertions.assertEquals(clientDTO.serviceId, observedClientSample.getServiceId());
        Assertions.assertEquals(clientDTO.mediaUnitId, observedClientSample.getMediaUnitId());

        Assertions.assertEquals(clientDTO.roomId, clientSample.roomId);
        Assertions.assertEquals(clientDTO.userId, clientSample.userId);

        Assertions.assertEquals(clientDTO.callId, clientSample.callId);
        Assertions.assertEquals(clientDTO.clientId, clientSample.clientId);

        Assertions.assertEquals(clientDTO.joined, clientSample.timestamp);
        Assertions.assertEquals(clientDTO.timeZoneId, observedClientSample.getTimeZoneId());

        Assertions.assertEquals(clientDTO.marker, clientSample.marker, "marker field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

}