package org.observertc.observer.components.depots;

import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * For single thread only!
 */
public class PeerConnectionDTOsDepot implements Supplier<Map<UUID, PeerConnectionDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionDTOsDepot.class);

    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.PeerConnectionTransport pcTransport;
    private Map<UUID, PeerConnectionDTO> buffer = new HashMap<>();

    public PeerConnectionDTOsDepot setObservedClientSample(ObservedClientSample value) {
        if (Objects.isNull(value)) return this;
        this.observedClientSample = value;
        return this;
    }

    public PeerConnectionDTOsDepot setPeerConnectionTransport(Samples.ClientSample.PeerConnectionTransport value) {
        if (Objects.isNull(value)) return this;
        this.pcTransport = value;
        return this;
    }

    private void clean() {
        this.observedClientSample = null;
        this.pcTransport = null;
    }

    public void assemble() {
        if (Objects.isNull(observedClientSample) || Objects.isNull(observedClientSample.getClientSample())) {
            logger.warn("No observed client sample");
            return;
        }
        if (this.buffer.containsKey(pcTransport)) {
            logger.warn("Cannot assemble {} without pcTransport", this.getClass().getSimpleName());
            return;
        }
        var clientSample = observedClientSample.getClientSample();
        try {
            var peerConnection = PeerConnectionDTO.builder()
                    .withCallId(clientSample.callId)
                    .withServiceId(observedClientSample.getServiceId())
                    .withRoomId(clientSample.roomId)

                    .withUserId(clientSample.userId)
                    .withMediaUnitId(observedClientSample.getMediaUnitId())

                    .withPeerConnectionId(pcTransport.peerConnectionId)
                    .withCreatedTimestamp(clientSample.timestamp)
                    .withClientId(clientSample.clientId)
                    .withMarker(clientSample.marker)
                    .build();
            this.buffer.put(pcTransport.peerConnectionId, peerConnection);
        } catch (Exception ex) {
            logger.warn("Error occurred while assembling {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }
    }

    @Override
    public Map<UUID, PeerConnectionDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
