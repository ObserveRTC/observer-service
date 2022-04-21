package org.observertc.observer.evaluators.depots;

import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.samples.ObservedClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * For single thread only!
 */
public class ClientDTOsDepot implements Supplier<Map<UUID, ClientDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(ClientDTOsDepot.class);

    private Map<UUID, ClientDTO> buffer = new HashMap<>();

    public ClientDTOsDepot addFromObservedClientSample(ObservedClientSample observedClientSample) {
        if (Objects.isNull(observedClientSample) || Objects.isNull(observedClientSample.getClientSample())) {
            logger.warn("No observed client sample");
        }
        var clientSample = observedClientSample.getClientSample();
        if (this.buffer.containsKey(clientSample.clientId)) {
            return this;
        }
        var clientDTO = ClientDTO.builder()
                .withServiceId(observedClientSample.getServiceId())
                .withMediaUnitId(observedClientSample.getMediaUnitId())
                .withRoomId(clientSample.roomId)
                .withCallId(clientSample.callId)
                .withClientId(clientSample.clientId)
                .withUserId(clientSample.userId)
                .withTimeZoneId(observedClientSample.getTimeZoneId())
                .withJoinedTimestamp(clientSample.timestamp)
                .withMarker(clientSample.marker)
                .build();
        this.buffer.put(clientSample.clientId, clientDTO);
        return this;
    }

    @Override
    public Map<UUID, ClientDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
