package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.utils.ObservedSamplesGenerator;

class SfuRtpPadDTOsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private final SfuRtpPadDTOsDepot depot = new SfuRtpPadDTOsDepot();

    @Test
    @Order(1)
    void shouldMakeDTO_1() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var sfuSample = observedSfuSample.getSfuSample();
        var outboundRtpPad = sfuSample.outboundRtpPads[0];
        depot
                .setObservedSfuSample(observedSfuSample)
                .setOutboundRtpPad(outboundRtpPad)
                .assemble();

        var sfuRtpPadDTO = depot.get().get(outboundRtpPad.padId);

        Assertions.assertEquals(sfuRtpPadDTO.serviceId, observedSfuSample.getServiceId());
        Assertions.assertEquals(sfuRtpPadDTO.mediaUnitId, observedSfuSample.getMediaUnitId());

        Assertions.assertEquals(sfuRtpPadDTO.sfuId, sfuSample.sfuId);
        Assertions.assertEquals(sfuRtpPadDTO.transportId, outboundRtpPad.transportId);
        Assertions.assertEquals(sfuRtpPadDTO.streamId, outboundRtpPad.streamId);
        Assertions.assertEquals(sfuRtpPadDTO.sinkId, outboundRtpPad.sinkId);
        Assertions.assertEquals(sfuRtpPadDTO.rtpPadId, outboundRtpPad.padId);
        Assertions.assertEquals(sfuRtpPadDTO.streamDirection, StreamDirection.OUTBOUND);
        Assertions.assertEquals(sfuRtpPadDTO.internal, outboundRtpPad.internal);
        Assertions.assertEquals(sfuRtpPadDTO.added, sfuSample.timestamp);
        Assertions.assertEquals(sfuRtpPadDTO.marker, sfuSample.marker, "marker field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

    @Test
    @Order(1)
    void shouldMakeDTO_2() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var sfuSample = observedSfuSample.getSfuSample();
        var inboundRtpPad = sfuSample.inboundRtpPads[0];
        depot
                .setObservedSfuSample(observedSfuSample)
                .setSfuInboundRtpPad(inboundRtpPad)
                .assemble();

        var sfuRtpPadDTO = depot.get().get(inboundRtpPad.padId);

        Assertions.assertEquals(sfuRtpPadDTO.serviceId, observedSfuSample.getServiceId());
        Assertions.assertEquals(sfuRtpPadDTO.mediaUnitId, observedSfuSample.getMediaUnitId());

        Assertions.assertEquals(sfuRtpPadDTO.sfuId, sfuSample.sfuId);
        Assertions.assertEquals(sfuRtpPadDTO.transportId, inboundRtpPad.transportId);
        Assertions.assertEquals(sfuRtpPadDTO.streamId, inboundRtpPad.streamId);
        Assertions.assertEquals(sfuRtpPadDTO.sinkId, null);
        Assertions.assertEquals(sfuRtpPadDTO.rtpPadId, inboundRtpPad.padId);
        Assertions.assertEquals(sfuRtpPadDTO.streamDirection, StreamDirection.INBOUND);
        Assertions.assertEquals(sfuRtpPadDTO.internal, inboundRtpPad.internal);
        Assertions.assertEquals(sfuRtpPadDTO.added, sfuSample.timestamp);
        Assertions.assertEquals(sfuRtpPadDTO.marker, sfuSample.marker, "marker field");

    }

}