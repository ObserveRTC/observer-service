package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.schemas.samples.Samples;

import java.time.Instant;
import java.util.UUID;

class CustomSfuEventReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private CustomSfuEventReportsDepot depot = new CustomSfuEventReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var serviceId = observedSfuSample.getServiceId();
        var mediaUnitId = observedSfuSample.getMediaUnitId();
        var sfuSample = observedSfuSample.getSfuSample();
        var name = UUID.randomUUID().toString();
        var timestamp = Instant.now().toEpochMilli();
        var value = UUID.randomUUID().toString();
        this.depot
                .setObservedSfuSample(observedSfuSample)
                .setCustomSfuEvent(Samples.SfuSample.CustomSfuEvent.newBuilder()
                        .setName(name)
                        .setTimestamp(timestamp)
                        .setValue(value)
                        .build())
                .assemble();

        var actual = depot.get().get(0);

        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuSample.marker, actual.marker, "marker field");
        Assertions.assertEquals(name, actual.name, "name field");
        Assertions.assertEquals(value, actual.value, "value field");
        Assertions.assertEquals(timestamp, actual.timestamp, "timestamp field");
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
        var timestamp = Instant.now().toEpochMilli();
        this.depot
                .setObservedSfuSample(observedSfuSample)
                .setCustomSfuEvent(Samples.SfuSample.CustomSfuEvent.newBuilder()
                        .setTimestamp(timestamp)
                        .build())
                .assemble();
        var actual = depot.get().get(0);

        Assertions.assertNull(actual.name, "name field");
        Assertions.assertNull(actual.value, "value field");
    }
}