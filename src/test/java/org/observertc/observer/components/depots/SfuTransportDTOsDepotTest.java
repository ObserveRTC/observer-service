package org.observertc.observer.components.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.samples.ObservedSamplesGenerator;

class SfuTransportDTOsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private final SfuTransportDTOsDepot depot = new SfuTransportDTOsDepot();

    @Test
    @Order(1)
    void shouldMakeDTO_1() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var sfuSample = observedSfuSample.getSfuSample();
        var sfuTransport = sfuSample.transports[0];
        depot
                .setObservedSfuSample(observedSfuSample)
                .setSfuTransport(sfuTransport)
                .assemble();

        var sfuTransportDTO = depot.get().get(sfuTransport.transportId);

        Assertions.assertEquals(sfuTransportDTO.serviceId, observedSfuSample.getServiceId());
        Assertions.assertEquals(sfuTransportDTO.mediaUnitId, observedSfuSample.getMediaUnitId());

        Assertions.assertEquals(sfuTransportDTO.sfuId, sfuSample.sfuId);
        Assertions.assertEquals(sfuTransportDTO.transportId, sfuTransportDTO.transportId);
        Assertions.assertEquals(sfuTransportDTO.internal, sfuTransportDTO.internal);
        Assertions.assertEquals(sfuTransportDTO.opened, sfuSample.timestamp);
        Assertions.assertEquals(sfuTransportDTO.marker, sfuSample.marker, "marker field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

}