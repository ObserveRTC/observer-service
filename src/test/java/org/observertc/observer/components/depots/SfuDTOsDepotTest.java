package org.observertc.observer.components.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.common.MinuteToTimeZoneOffsetConverter;
import org.observertc.observer.samples.ObservedSamplesGenerator;

class SfuDTOsDepotTest {

    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private final SfuDTOsDepot depot = new SfuDTOsDepot();

    @Test
    @Order(1)
    void shouldMakeDTO() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var converter = new MinuteToTimeZoneOffsetConverter();
        depot.addFromObservedSfuSample(observedSfuSample);
        var sfuSample = observedSfuSample.getSfuSample();
        var sfuDTO = depot.get().get(sfuSample.sfuId);

        Assertions.assertEquals(sfuDTO.serviceId, observedSfuSample.getServiceId());
        Assertions.assertEquals(sfuDTO.mediaUnitId, observedSfuSample.getMediaUnitId());

        Assertions.assertEquals(sfuDTO.sfuId, sfuSample.sfuId);
        Assertions.assertEquals(sfuDTO.joined, sfuSample.timestamp);

        Assertions.assertEquals(sfuDTO.timeZoneId, observedSfuSample.getTimeZoneId());
        Assertions.assertEquals(sfuDTO.marker, sfuSample.marker, "marker field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }
}