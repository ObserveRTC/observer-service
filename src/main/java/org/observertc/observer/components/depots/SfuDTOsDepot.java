package org.observertc.observer.components.depots;

import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.samples.ObservedSfuSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * For single thread only!
 */
public class SfuDTOsDepot implements Supplier<Map<UUID, SfuDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(SfuDTOsDepot.class);

    private Map<UUID, SfuDTO> buffer = new HashMap<>();

    public SfuDTOsDepot addFromObservedClientSample(ObservedSfuSample observedSfuSample) {
        if (Objects.isNull(observedSfuSample) || Objects.isNull(observedSfuSample.getSfuSample())) {
            logger.warn("No observed client sample");
        }
        var sfuSample = observedSfuSample.getSfuSample();
        if (this.buffer.containsKey(sfuSample.sfuId)) {
            return this;
        }
        var sfuDTO = SfuDTO.builder()
                .withSfuId(sfuSample.sfuId)
                .withServiceId(observedSfuSample.getServiceId())
                .withMediaUnitId(observedSfuSample.getMediaUnitId())
                .withTimeZoneId(observedSfuSample.getTimeZoneId())
                .withConnectedTimestamp(sfuSample.timestamp)
                .build();
        this.buffer.put(sfuDTO.sfuId, sfuDTO);
        return this;
    }

    @Override
    public Map<UUID, SfuDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
