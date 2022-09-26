package org.observertc.observer.evaluators.depots;

import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.PeerConnectionTransportReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class PeerConnectionTransportReportsDepot implements Supplier<List<PeerConnectionTransportReport>> {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionTransportReportsDepot.class);

    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.PeerConnectionTransport peerConnectionTransport = null;
    private List<PeerConnectionTransportReport> buffer = new LinkedList<>();


    public PeerConnectionTransportReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public PeerConnectionTransportReportsDepot setPeerConnectionTransport(Samples.ClientSample.PeerConnectionTransport value) {
        this.peerConnectionTransport = value;
        return this;
    }

    private PeerConnectionTransportReportsDepot clean() {
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

            var report = PeerConnectionTransportReport.newBuilder()
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
                    .setPeerConnectionId(peerConnectionTransport.peerConnectionId)
                    .setLabel(peerConnectionTransport.label)

                    /* Transport stats */
                    .setPeerConnectionId(peerConnectionTransport.peerConnectionId)
                    .setLabel(peerConnectionTransport.label)
                    .setSampleSeq(clientSample.sampleSeq)
                    .setPacketsSent(peerConnectionTransport.packetsSent)
                    .setPacketsReceived(peerConnectionTransport.packetsReceived)
                    .setBytesSent(peerConnectionTransport.bytesSent)
                    .setBytesReceived(peerConnectionTransport.bytesReceived)
                    .setIceRole(peerConnectionTransport.iceRole)
                    .setIceLocalUsernameFragment(peerConnectionTransport.iceLocalUsernameFragment)
                    .setDtlsState(peerConnectionTransport.dtlsState)
                    .setSelectedCandidatePairId(peerConnectionTransport.selectedCandidatePairId)
                    .setIceState(peerConnectionTransport.iceState)
                    .setLocalCertificateId(peerConnectionTransport.localCertificateId)
                    .setRemoteCertificateId(peerConnectionTransport.remoteCertificateId)
                    .setTlsVersion(peerConnectionTransport.tlsVersion)
                    .setDtlsCipher(peerConnectionTransport.dtlsCipher)
                    .setSrtpCipher(peerConnectionTransport.srtpCipher)
                    .setTlsGroup(peerConnectionTransport.tlsGroup)
                    .setSelectedCandidatePairChanges(peerConnectionTransport.selectedCandidatePairChanges)

                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<PeerConnectionTransportReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
