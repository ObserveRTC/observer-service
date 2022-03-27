package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.ClientTransportReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ClientTransportReportsDepot implements Supplier<List<ClientTransportReport>> {

    private static final Logger logger = LoggerFactory.getLogger(ClientTransportReportsDepot.class);

    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.PeerConnectionTransport peerConnectionTransport = null;
    private List<ClientTransportReport> buffer = new LinkedList<>();


    public ClientTransportReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public ClientTransportReportsDepot setPeerConnectionTransport(Samples.ClientSample.PeerConnectionTransport value) {
        this.peerConnectionTransport = value;
        return this;
    }

    private ClientTransportReportsDepot clean() {
        this.observedClientSample = null;
        this.peerConnectionTransport = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(peerConnectionTransport)) {
                logger.warn("Cannot assemble {} without peerConnectionTransport", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            String callId = UUIDAdapter.toStringOrNull(clientSample.callId);
            String clientId = UUIDAdapter.toStringOrNull(clientSample.clientId);

            String peerConnectionId = UUIDAdapter.toStringOrNull(peerConnectionTransport.peerConnectionId);
            var report = ClientTransportReport.newBuilder()
                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(clientSample.marker)
                    .setTimestamp(clientSample.timestamp)

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(clientSample.roomId)
                    .setClientId(clientId)
                    .setUserId(clientSample.userId)
                    .setPeerConnectionId(peerConnectionId)
                    .setLabel(peerConnectionTransport.label)

                    /* Transport stats */
                    .setPacketsSent(peerConnectionTransport.packetsSent)
                    .setPacketsReceived(peerConnectionTransport.packetsReceived)
                    .setBytesSent(peerConnectionTransport.bytesSent)
                    .setBytesReceived(peerConnectionTransport.bytesReceived)
                    .setIceRole(peerConnectionTransport.iceRole)
                    .setIceLocalUsernameFragment(peerConnectionTransport.iceLocalUsernameFragment)
                    .setDtlsState(peerConnectionTransport.dtlsState)
                    .setIceTransportState(peerConnectionTransport.iceState)
                    .setTlsVersion(peerConnectionTransport.tlsVersion)
                    .setDtlsCipher(peerConnectionTransport.dtlsCipher)
                    .setSrtpCipher(peerConnectionTransport.srtpCipher)
                    .setTlsGroup(peerConnectionTransport.tlsGroup)
                    .setSelectedCandidatePairChanges(peerConnectionTransport.selectedCandidatePairChanges)

                    /* ICE Local Candidate */
                    .setLocalAddress(peerConnectionTransport.localAddress)
                    .setLocalPort(peerConnectionTransport.localPort)
                    .setLocalProtocol(peerConnectionTransport.localProtocol)
                    .setLocalCandidateType(peerConnectionTransport.localCandidateType)
                    .setLocalCandidateICEServerUrl(peerConnectionTransport.localCandidateICEServerUrl)
                    .setLocalCandidateRelayProtocol(peerConnectionTransport.localCandidateRelayProtocol)

                    /* ICE Remote Candidate */
                    .setRemoteAddress(peerConnectionTransport.remoteAddress)
                    .setRemotePort(peerConnectionTransport.remotePort)
                    .setRemoteProtocol(peerConnectionTransport.remoteProtocol)
                    .setRemoteCandidateType(peerConnectionTransport.remoteCandidateType)
                    .setRemoteCandidateICEServerUrl(peerConnectionTransport.remoteCandidateICEServerUrl)
                    .setRemoteCandidateRelayProtocol(peerConnectionTransport.remoteCandidateRelayProtocol)

                    /* ICE Candidate Pair*/
                    .setCandidatePairState(peerConnectionTransport.candidatePairState)
                    .setCandidatePairPacketsSent(peerConnectionTransport.candidatePairPacketsSent)
                    .setCandidatePairPacketsReceived(peerConnectionTransport.candidatePairPacketsReceived)
                    .setCandidatePairBytesSent(peerConnectionTransport.candidatePairBytesSent)
                    .setCandidatePairBytesReceived(peerConnectionTransport.bytesReceived)
                    .setCandidatePairLastPacketSentTimestamp(peerConnectionTransport.candidatePairLastPacketSentTimestamp)
                    .setCandidatePairLastPacketReceivedTimestamp(peerConnectionTransport.candidatePairLastPacketReceivedTimestamp)
                    .setCandidatePairFirstRequestTimestamp(peerConnectionTransport.candidatePairFirstRequestTimestamp)
                    .setCandidatePairLastRequestTimestamp(peerConnectionTransport.candidatePairLastRequestTimestamp)
                    .setCandidatePairTotalRoundTripTime(peerConnectionTransport.candidatePairTotalRoundTripTime)
                    .setCandidatePairCurrentRoundTripTime(peerConnectionTransport.candidatePairCurrentRoundTripTime)
                    .setCandidatePairAvailableOutgoingBitrate(peerConnectionTransport.candidatePairAvailableOutgoingBitrate)
                    .setCandidatePairAvailableIncomingBitrate(peerConnectionTransport.candidatePairAvailableOutgoingBitrate)
                    .setCandidatePairCircuitBreakerTriggerCount(peerConnectionTransport.candidatePairCircuitBreakerTriggerCount)
                    .setCandidatePairRequestsReceived(peerConnectionTransport.candidatePairRequestsReceived)
                    .setCandidatePairRequestsSent(peerConnectionTransport.candidatePairRequestsSent)
                    .setCandidatePairResponsesReceived(peerConnectionTransport.candidatePairResponsesReceived)
                    .setCandidatePairRetransmissionReceived(peerConnectionTransport.candidatePairRetransmissionReceived)
                    .setCandidatePairRetransmissionSent(peerConnectionTransport.candidatePairRetransmissionSent)
                    .setCandidatePairConsentRequestsSent(peerConnectionTransport.candidatePairConsentRequestsSent)
                    .setCandidatePairConsentExpiredTimestamp(peerConnectionTransport.candidatePairConsentExpiredTimestamp)
                    .setCandidatePairPacketsDiscardedOnSend(peerConnectionTransport.candidatePairPacketsDiscardedOnSend)
                    .setCandidatePairBytesDiscardedOnSend(peerConnectionTransport.candidatePairBytesDiscardedOnSend)
                    .setCandidatePairRequestBytesSent(peerConnectionTransport.candidatePairRequestBytesSent)
                    .setCandidatePairConsentRequestBytesSent(peerConnectionTransport.candidatePairConsentRequestBytesSent)
                    .setCandidatePairResponseBytesSent(peerConnectionTransport.candidatePairResponseBytesSent)

                    /* SCTP stats */
                    .setSctpSmoothedRoundTripTime(peerConnectionTransport.sctpSmoothedRoundTripTime)
                    .setSctpCongestionWindow(peerConnectionTransport.sctpCongestionWindow)
                    .setSctpReceiverWindow(peerConnectionTransport.sctpReceiverWindow)
                    .setSctpMtu(peerConnectionTransport.sctpMtu)
                    .setSctpUnackData(peerConnectionTransport.sctpUnackData)

                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<ClientTransportReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
