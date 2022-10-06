package org.observertc.observer.evaluators.depots;

import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.IceCandidatePairReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class IceCandidatePairReportsDepot implements Supplier<List<IceCandidatePairReport>> {

    private static final Logger logger = LoggerFactory.getLogger(IceCandidatePairReportsDepot.class);

    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.IceCandidatePair iceCandidatePair = null;
    private List<IceCandidatePairReport> buffer = new LinkedList<>();


    public IceCandidatePairReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public IceCandidatePairReportsDepot setIceCandidatePair(Samples.ClientSample.IceCandidatePair value) {
        this.iceCandidatePair = value;
        return this;
    }

    private IceCandidatePairReportsDepot clean() {
        this.observedClientSample = null;
        this.iceCandidatePair = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(iceCandidatePair)) {
                logger.warn("Cannot assemble {} without iceCandidatePair", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();

            var report = IceCandidatePairReport.newBuilder()
                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(clientSample.marker)
                    .setTimestamp(clientSample.timestamp)

                    /* Peer Connection Report Fields */
                    .setCallId(clientSample.callId)
                    .setRoomId(clientSample.roomId)
                    .setClientId(clientSample.clientId)
                    .setUserId(clientSample.userId)
                    .setPeerConnectionId(iceCandidatePair.peerConnectionId)

                    /* ICE Candidate Pair*/
                    .setPeerConnectionId(iceCandidatePair.peerConnectionId)
                    .setLabel(iceCandidatePair.label)
                    .setSampleSeq(clientSample.sampleSeq)
                    .setTransportId(iceCandidatePair.transportId)
                    .setLocalCandidateId(iceCandidatePair.localCandidateId)
                    .setRemoteCandidateId(iceCandidatePair.remoteCandidateId)
                    .setState(iceCandidatePair.state)
                    .setNominated(iceCandidatePair.nominated)
                    .setPacketsSent(iceCandidatePair.packetsSent)
                    .setPacketsReceived(iceCandidatePair.packetsReceived)
                    .setBytesSent(iceCandidatePair.bytesSent)
                    .setBytesReceived(iceCandidatePair.bytesReceived)
                    .setLastPacketSentTimestamp(iceCandidatePair.lastPacketSentTimestamp)
                    .setLastPacketReceivedTimestamp(iceCandidatePair.lastPacketReceivedTimestamp)
                    .setTotalRoundTripTime(iceCandidatePair.totalRoundTripTime)
                    .setCurrentRoundTripTime(iceCandidatePair.currentRoundTripTime)
                    .setAvailableOutgoingBitrate(iceCandidatePair.availableOutgoingBitrate)
                    .setAvailableIncomingBitrate(iceCandidatePair.availableIncomingBitrate)
                    .setRequestsReceived(iceCandidatePair.requestsReceived)
                    .setRequestsSent(iceCandidatePair.requestsSent)
                    .setResponsesReceived(iceCandidatePair.responsesReceived)
                    .setResponsesSent(iceCandidatePair.responsesSent)
                    .setConsentRequestsSent(iceCandidatePair.consentRequestsSent)
                    .setPacketsDiscardedOnSend(iceCandidatePair.packetsDiscardedOnSend)
                    .setBytesDiscardedOnSend(iceCandidatePair.bytesDiscardedOnSend)
                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<IceCandidatePairReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
