package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.ClientDataChannelReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ClientDataChannelReportsDepot implements Supplier<List<ClientDataChannelReport>> {

    private static final Logger logger = LoggerFactory.getLogger(ClientDataChannelReportsDepot.class);

    private String peerConnectionLabel = null;
    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.DataChannel dataChannel = null;
    private List<ClientDataChannelReport> buffer = new LinkedList<>();

    public ClientDataChannelReportsDepot setPeerConnectionLabel(String value) {
        this.peerConnectionLabel = value;
        return this;
    }

    public ClientDataChannelReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public ClientDataChannelReportsDepot setDataChannel(Samples.ClientSample.DataChannel value) {
        this.dataChannel = value;
        return this;
    }

    private ClientDataChannelReportsDepot clean() {
        this.observedClientSample = null;
        this.dataChannel = null;
        this.peerConnectionLabel = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(dataChannel)) {
                logger.warn("Cannot assemble {} without dataChannel", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            String callId = UUIDAdapter.toStringOrNull(clientSample.callId);
            String clientId = UUIDAdapter.toStringOrNull(clientSample.clientId);

            String peerConnectionId = UUIDAdapter.toStringOrNull(dataChannel.peerConnectionId);
            var report = ClientDataChannelReport.newBuilder()
                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(clientSample.marker)
                    .setTimestamp(clientSample.timestamp)

                    /* Peer Connection Report Fields */
                    .setCallId(callId)
                    .setRoomId(clientSample.roomId)
                    .setClientId(clientId)
                    .setUserId(clientSample.userId)
                    .setPeerConnectionId(peerConnectionId)
                    .setPeerConnectionLabel(peerConnectionLabel)

                    /* Sample Based Report Fields */
                    .setSampleSeq(clientSample.sampleSeq)

                    /* Data Channel stats */
                    .setLabel(dataChannel.label)
                    .setProtocol(dataChannel.protocol)
                    .setMessagesSent(dataChannel.messagesSent)
                    .setBytesSent(dataChannel.bytesSent)
                    .setMessagesReceived(dataChannel.messagesReceived)
                    .setBytesReceived(dataChannel.bytesReceived)
                    .setState(dataChannel.state)
                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }
    }

    @Override
    public List<ClientDataChannelReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
