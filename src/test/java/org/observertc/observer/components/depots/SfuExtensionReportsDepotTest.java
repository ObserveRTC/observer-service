package org.observertc.observer.components.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.UUID;

class SfuExtensionReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private SfuExtensionReportsDepot depot = new SfuExtensionReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var sfuSample = observedSfuSample.getSfuSample();
        var serviceId = observedSfuSample.getServiceId();
        var mediaUnitId = observedSfuSample.getMediaUnitId();
        var extensionType = UUID.randomUUID().toString();
        var payload = UUID.randomUUID().toString();
        this.depot
                .setObservedSfuSample(observedSfuSample)
                .setPayload(payload)
                .setExtensionType(extensionType)
                .assemble();

        var actual = depot.get().get(0);

        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuSample.marker, actual.marker, "marker field");
        Assertions.assertEquals(sfuSample.timestamp, actual.timestamp, "timestamp field");
        Assertions.assertEquals(sfuSample.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(extensionType, actual.extensionType, "extensionType field");
        Assertions.assertEquals(payload, actual.payload, "payload field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

    @Test
    @Order(3)
    void shouldBeCleaned() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var extensionType = UUID.randomUUID().toString();
        this.depot
                .setObservedSfuSample(observedSfuSample)
                .setExtensionType(extensionType)
                .assemble();
        var actual = depot.get().get(0);

        Assertions.assertNull(actual.payload, "payload field");
    }
}