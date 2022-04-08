package org.observertc.observer.components.depots;

import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * For single thread only!
 */
public class SfuTransportDTOsDepot implements Supplier<Map<UUID, SfuTransportDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportDTOsDepot.class);

    private Map<UUID, SfuTransportDTO> buffer = new HashMap<>();

    private Samples.SfuSample.SfuTransport sfuTransport = null;
    private ObservedSfuSample observedSfuSample;


    public SfuTransportDTOsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public SfuTransportDTOsDepot setSfuTransport(Samples.SfuSample.SfuTransport value) {
        this.sfuTransport = value;
        return this;
    }

    private void clean() {
        this.observedSfuSample = null;
        this.sfuTransport = null;
    }


    public void assemble() {
        try {
            if (Objects.isNull(observedSfuSample)) {
                logger.warn("Cannot make an SfuTransportDTO without an observedSfuSample");
                return;
            }
            if (Objects.isNull(sfuTransport)) {
                logger.warn("Cannot make an SfuTransportDTO without an sfuTransport");
                return;
            }
            var sfuSample = observedSfuSample.getSfuSample();
            if (this.buffer.containsKey(sfuTransport.transportId)) {
                return;
            }
            var sfuTransportDTO = SfuTransportDTO.builder()
                    .withTransportId(sfuTransport.transportId)
                    .withSfuId(sfuSample.sfuId)
                    .withServiceId(observedSfuSample.getServiceId())
                    .withMediaUnitId(observedSfuSample.getMediaUnitId())
                    .withInternal(sfuTransport.internal)
                    .withOpenedTimestamp(sfuSample.timestamp)
                    .withMarker(sfuSample.marker)
                    .build();
            this.buffer.put(sfuTransportDTO.transportId, sfuTransportDTO);
        } catch (Exception ex){
            logger.warn("Exception occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public Map<UUID, SfuTransportDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
