package org.observertc.observer.components.depots;

import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.samples.ClientSampleVisitor;
import org.observertc.observer.samples.ObservedClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * For single thread only!
 */
public class PeerConnectionDTOsDepot implements Supplier<Map<UUID, PeerConnectionDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionDTOsDepot.class);

    private Map<UUID, PeerConnectionDTO> buffer = new HashMap<>();

    public PeerConnectionDTOsDepot addFromObservedClientSample(ObservedClientSample observedClientSample) {
        if (Objects.isNull(observedClientSample) || Objects.isNull(observedClientSample.getClientSample())) {
            logger.warn("No observed client sample");
        }
        var clientSample = observedClientSample.getClientSample();
        ClientSampleVisitor.streamPeerConnectionTransports(clientSample).forEach(pcTransport -> {
            if (this.buffer.containsKey(pcTransport.peerConnectionId)) {
                return;
            }
            var peerConnection = PeerConnectionDTO.builder()
                    .withCallId(clientSample.callId)
                    .withServiceId(observedClientSample.getServiceId())
                    .withRoomId(clientSample.roomId)

                    .withUserId(clientSample.userId)
                    .withMediaUnitId(observedClientSample.getMediaUnitId())

                    .withPeerConnectionId(pcTransport.peerConnectionId)
                    .withCreatedTimestamp(clientSample.timestamp)
                    .withClientId(clientSample.clientId)

                    .build();
            this.buffer.put(pcTransport.peerConnectionId, peerConnection);
        });
        return this;
    }

    @Override
    public Map<UUID, PeerConnectionDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
