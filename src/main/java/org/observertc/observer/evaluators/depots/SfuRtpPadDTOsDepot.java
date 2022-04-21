package org.observertc.observer.evaluators.depots;

import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * For single thread only!
 */
public class SfuRtpPadDTOsDepot implements Supplier<Map<UUID, SfuRtpPadDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadDTOsDepot.class);

    private Map<UUID, SfuRtpPadDTO> buffer = new HashMap<>();

    private Samples.SfuSample.SfuInboundRtpPad sfuInboundRtpPad = null;
    private Samples.SfuSample.SfuOutboundRtpPad sfuOutboundRtpPad = null;
    private ObservedSfuSample observedSfuSample;


    public SfuRtpPadDTOsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public SfuRtpPadDTOsDepot setSfuInboundRtpPad(Samples.SfuSample.SfuInboundRtpPad value) {
        this.sfuInboundRtpPad = value;
        return this;
    }

    public SfuRtpPadDTOsDepot setOutboundRtpPad(Samples.SfuSample.SfuOutboundRtpPad value) {
        this.sfuOutboundRtpPad = value;
        return this;
    }

    private void clean() {
        this.observedSfuSample = null;
        this.sfuInboundRtpPad = null;
        this.sfuOutboundRtpPad = null;
    }


    public void assemble() {
        try {
            if (Objects.isNull(observedSfuSample)) {
                logger.warn("Cannot make an SfuTransportDTO without an observedSfuSample");
                return;
            }
            if (Objects.isNull(sfuInboundRtpPad) && Objects.isNull(sfuOutboundRtpPad)) {
                logger.warn("Cannot make an SfuRtpPadDTO without sfuInboundRtpPad or sfuOutboundRtpPad");
                return;
            }
            var sfuSample = observedSfuSample.getSfuSample();
            UUID transportId;
            UUID rtpPadId;
            boolean internal;
            UUID streamId;
            UUID sinkId;
            StreamDirection direction;
            if (Objects.nonNull(sfuInboundRtpPad)) {
                transportId = sfuInboundRtpPad.transportId;
                rtpPadId = sfuInboundRtpPad.padId;
                internal = Boolean.TRUE.equals(sfuInboundRtpPad.internal);
                streamId = sfuInboundRtpPad.streamId;
                sinkId = null;
                direction = StreamDirection.INBOUND;
            } else {
                transportId = sfuOutboundRtpPad.transportId;
                rtpPadId = sfuOutboundRtpPad.padId;
                internal = sfuOutboundRtpPad.internal;
                streamId = sfuOutboundRtpPad.streamId;
                sinkId = sfuOutboundRtpPad.sinkId;
                direction = StreamDirection.OUTBOUND;
            }
            if (this.buffer.containsKey(rtpPadId)) {
                return;
            }
            var sfuRtpPadDTO = SfuRtpPadDTO.builder()
                    .withSfuId(sfuSample.sfuId)
                    .withServiceId(observedSfuSample.getServiceId())
                    .withMediaUnitId(observedSfuSample.getMediaUnitId())
                    .withSfuTransportId(transportId)
                    .withSfuRtpPadId(rtpPadId)
                    .withInternal(internal)
                    .withStreamId(streamId)
                    .withSinkId(sinkId)
                    .withStreamDirection(direction)
                    .withAddedTimestamp(sfuSample.timestamp)
                    .withMarker(sfuSample.marker)
                    .build();
            this.buffer.put(sfuRtpPadDTO.rtpPadId, sfuRtpPadDTO);
        } catch (Exception ex){
            logger.warn("Exception occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public Map<UUID, SfuRtpPadDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
