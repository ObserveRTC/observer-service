package org.observertc.observer.sinks.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.micronaut.context.annotation.Prototype;
import org.bson.Document;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configs.InvalidConfigurationException;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.observer.reports.ReportTypeVisitors;
import org.observertc.observer.sinks.Sink;
import org.observertc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class MongoSinkBuilder extends AbstractBuilder implements Builder<Sink> {
    private final static Logger logger = LoggerFactory.getLogger(MongoSinkBuilder.class);

    private static Map<ReportType, String> getDefaultCollectionNames() {
        final Map<ReportType, String> result = new HashMap<>();
        result.put(ReportType.OBSERVER_EVENT, ObserverEventReport.class.getSimpleName() + "s");
        result.put(ReportType.CLIENT_EXTENSION_DATA, ClientExtensionReport.class.getSimpleName() + "s");
        result.put(ReportType.CALL_EVENT, CallEventReport.class.getSimpleName() + "s");
        result.put(ReportType.CALL_META_DATA, CallMetaReport.class.getSimpleName() + "s");
        result.put(ReportType.INBOUND_AUDIO_TRACK, InboundAudioTrackReport.class.getSimpleName() + "s");
        result.put(ReportType.INBOUND_VIDEO_TRACK, InboundVideoTrackReport.class.getSimpleName() + "s");
        result.put(ReportType.OUTBOUND_AUDIO_TRACK, OutboundAudioTrackReport.class.getSimpleName() + "s");
        result.put(ReportType.OUTBOUND_VIDEO_TRACK, OutboundVideoTrackReport.class.getSimpleName() + "s");
        result.put(ReportType.PEER_CONNECTION_DATA_CHANNEL, ClientDataChannelReport.class.getSimpleName() + "s");
        result.put(ReportType.PEER_CONNECTION_TRANSPORT, ClientTransportReport.class.getSimpleName() + "s");

        result.put(ReportType.SFU_EVENT, SfuEventReport.class.getSimpleName() + "s");
        result.put(ReportType.SFU_META_DATA, SfuMetaReport.class.getSimpleName() + "s");
        result.put(ReportType.SFU_TRANSPORT, SFUTransportReport.class.getSimpleName() + "s");
        result.put(ReportType.SFU_INBOUND_RTP_PAD, SfuInboundRtpPadReport.class.getSimpleName() + "s");
        result.put(ReportType.SFU_OUTBOUND_RTP_PAD, SfuOutboundRtpPadReport.class.getSimpleName() + "s");
        result.put(ReportType.SFU_SCTP_STREAM, SfuSctpStreamReport.class.getSimpleName() + "s");
        return result;
    }

    @Override
    public void set(Object subject) {
        logger.warn("Unrecognized subject {}", subject.getClass().getSimpleName());
    }

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        if (config.uri == null && config.connection == null) {
            throw new InvalidConfigurationException("uri or connection config must be given");
        } else if (config.uri != null && config.connection != null) {
            throw new InvalidConfigurationException("either the uri or the connection config must be given, not both");
        }
        ConnectionString connectionString;
        if (config.uri != null) {
            connectionString = new ConnectionString(config.uri);
        } else {
            var builder = new ConnectionStringBuilder();
            builder.withConfiguration(config.connection);
            connectionString = builder.build();
        }
        var clientProvider = this.makeClientProvider(connectionString);
        var documentMapper = this.makeReportMapper();
        var documentSorter = this.makeSorter(config.savingStrategy);
        MongoSink result;
        try {
            result = new MongoSink(
                    config.printSummary,
                    clientProvider,
                    config.database,
                    documentMapper,
                    documentSorter
            );
        } catch (Throwable t) {
            logger.error("cannot make mongo sink", t);
            return null;
        }
        return result;
    }

    private Supplier<MongoClient> makeClientProvider(ConnectionString connectionString) {
        AtomicReference<MongoClient> clientHolder = new AtomicReference<>();
        return () -> {
            var client = clientHolder.get();
            if (client == null) {
                try {
                    client = MongoClients.create(connectionString);
                } catch (Throwable t) {
                    logger.error("cannot make mongo client", t);
                    return null;
                }
                clientHolder.set(client);
            }
            return client;
        };
    }

    private Function<List<Report>, Map<String, List<Report>>> makeSorter(Config.SavingStrategy strategy) {
        switch (strategy) {
            case ONE_COLLECTION -> {
                return reports -> Map.of("reports", reports);
            }
            case SERVICE_ID_BASED -> {
                var serviceIdVisitor = ReportTypeVisitors.serviceIdGetter();
                return reports -> reports.stream().collect(groupingBy(report -> serviceIdVisitor.apply(report.payload, report.type)));
            }
            case REPORT_TYPE_BASED -> {
                var collectionNames = getDefaultCollectionNames();
                return reports -> reports.stream().collect(groupingBy(report -> collectionNames.get(report.type)));
            }
        }
        throw new RuntimeException("Strategy has not been recognized");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Config {

        public enum SavingStrategy {
            REPORT_TYPE_BASED,
            SERVICE_ID_BASED,
            ONE_COLLECTION,
        }

        public String uri;

        public Map<String, Object> connection;

        @NotNull
        public String database;

        public SavingStrategy savingStrategy = SavingStrategy.ONE_COLLECTION;

        public Map<ReportType, String> collectionNames = null;

        public boolean printSummary = false;



    }

    private Function<Report, Document> makeReportMapper() {
        var payloadMapper = ReportTypeVisitor.<Object, Document>createFunctionalVisitor(
                payload -> {
                    var reportPayload = ((ObserverEventReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("sampleTimestamp", reportPayload.sampleTimestamp)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("name", reportPayload.name)
                            .append("message", reportPayload.message)
                            .append("value", reportPayload.value)
                            .append("attachments", reportPayload.attachments);
                },
                payload -> {
                    var reportPayload = ((CallEventReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("mediaTrackId", reportPayload.mediaTrackId)
                            .append("SSRC", reportPayload.SSRC)
                            .append("sampleTimestamp", reportPayload.sampleTimestamp)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("name", reportPayload.name)
                            .append("message", reportPayload.message)
                            .append("value", reportPayload.value)
                            .append("attachments", reportPayload.attachments);
                },
                payload -> {
                    var reportPayload = ((CallMetaReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("sampleTimestamp", reportPayload.sampleTimestamp)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("type", reportPayload.type)
                            .append("payload", reportPayload.payload);
                },
                payload -> {
                    var reportPayload = ((ClientExtensionReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("extensionType", reportPayload.extensionType)
                            .append("payload", reportPayload.payload);
                },payload -> {
                    var reportPayload = ((ClientTransportReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("label", reportPayload.label)
                            .append("packetsSent", reportPayload.packetsSent)
                            .append("packetsReceived", reportPayload.packetsReceived)
                            .append("bytesSent", reportPayload.bytesSent)
                            .append("bytesReceived", reportPayload.bytesReceived)
                            .append("iceRole", reportPayload.iceRole)
                            .append("iceLocalUsernameFragment", reportPayload.iceLocalUsernameFragment)
                            .append("dtlsState", reportPayload.dtlsState)
                            .append("iceTransportState", reportPayload.iceTransportState)
                            .append("tlsVersion", reportPayload.tlsVersion)
                            .append("dtlsCipher", reportPayload.dtlsCipher)
                            .append("srtpCipher", reportPayload.srtpCipher)
                            .append("tlsGroup", reportPayload.tlsGroup)
                            .append("selectedCandidatePairChanges", reportPayload.selectedCandidatePairChanges)
                            .append("localAddress", reportPayload.localAddress)
                            .append("localPort", reportPayload.localPort)
                            .append("localProtocol", reportPayload.localProtocol)
                            .append("localCandidateType", reportPayload.localCandidateType)
                            .append("localCandidateICEServerUrl", reportPayload.localCandidateICEServerUrl)
                            .append("localCandidateRelayProtocol", reportPayload.localCandidateRelayProtocol)
                            .append("remoteAddress", reportPayload.remoteAddress)
                            .append("remotePort", reportPayload.remotePort)
                            .append("remoteProtocol", reportPayload.remoteProtocol)
                            .append("remoteCandidateType", reportPayload.remoteCandidateType)
                            .append("remoteCandidateICEServerUrl", reportPayload.remoteCandidateICEServerUrl)
                            .append("remoteCandidateRelayProtocol", reportPayload.remoteCandidateRelayProtocol)
                            .append("candidatePairState", reportPayload.candidatePairState)
                            .append("candidatePairPacketsSent", reportPayload.candidatePairPacketsSent)
                            .append("candidatePairPacketsReceived", reportPayload.candidatePairPacketsReceived)
                            .append("candidatePairBytesSent", reportPayload.candidatePairBytesSent)
                            .append("candidatePairBytesReceived", reportPayload.candidatePairBytesReceived)
                            .append("candidatePairLastPacketSentTimestamp", reportPayload.candidatePairLastPacketSentTimestamp)
                            .append("candidatePairLastPacketReceivedTimestamp", reportPayload.candidatePairLastPacketReceivedTimestamp)
                            .append("candidatePairFirstRequestTimestamp", reportPayload.candidatePairFirstRequestTimestamp)
                            .append("candidatePairLastRequestTimestamp", reportPayload.candidatePairLastRequestTimestamp)
                            .append("candidatePairLastResponseTimestamp", reportPayload.candidatePairLastResponseTimestamp)
                            .append("candidatePairTotalRoundTripTime", reportPayload.candidatePairTotalRoundTripTime)
                            .append("candidatePairCurrentRoundTripTime", reportPayload.candidatePairCurrentRoundTripTime)
                            .append("candidatePairAvailableOutgoingBitrate", reportPayload.candidatePairAvailableOutgoingBitrate)
                            .append("candidatePairAvailableIncomingBitrate", reportPayload.candidatePairAvailableIncomingBitrate)
                            .append("candidatePairCircuitBreakerTriggerCount", reportPayload.candidatePairCircuitBreakerTriggerCount)
                            .append("candidatePairRequestsReceived", reportPayload.candidatePairRequestsReceived)
                            .append("candidatePairRequestsSent", reportPayload.candidatePairRequestsSent)
                            .append("candidatePairResponsesReceived", reportPayload.candidatePairResponsesReceived)
                            .append("candidatePairResponsesSent", reportPayload.candidatePairResponsesSent)
                            .append("candidatePairRetransmissionReceived", reportPayload.candidatePairRetransmissionReceived)
                            .append("candidatePairRetransmissionSent", reportPayload.candidatePairRetransmissionSent)
                            .append("candidatePairConsentRequestsSent", reportPayload.candidatePairConsentRequestsSent)
                            .append("candidatePairConsentExpiredTimestamp", reportPayload.candidatePairConsentExpiredTimestamp)
                            .append("candidatePairBytesDiscardedOnSend", reportPayload.candidatePairBytesDiscardedOnSend)
                            .append("candidatePairPacketsDiscardedOnSend", reportPayload.candidatePairPacketsDiscardedOnSend)
                            .append("candidatePairRequestBytesSent", reportPayload.candidatePairRequestBytesSent)
                            .append("candidatePairConsentRequestBytesSent", reportPayload.candidatePairConsentRequestBytesSent)
                            .append("candidatePairResponseBytesSent", reportPayload.candidatePairResponseBytesSent)
                            .append("sctpSmoothedRoundTripTime", reportPayload.sctpSmoothedRoundTripTime)
                            .append("sctpCongestionWindow", reportPayload.sctpCongestionWindow)
                            .append("sctpReceiverWindow", reportPayload.sctpReceiverWindow)
                            .append("sctpMtu", reportPayload.sctpMtu)
                            .append("sctpUnackData", reportPayload.sctpUnackData);
                },
                payload -> {
                    var reportPayload = ((ClientDataChannelReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("peerConnectionLabel", reportPayload.peerConnectionLabel)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("label", reportPayload.label)
                            .append("protocol", reportPayload.protocol)
                            .append("state", reportPayload.state)
                            .append("messagesSent", reportPayload.messagesSent)
                            .append("bytesSent", reportPayload.bytesSent)
                            .append("messagesReceived", reportPayload.messagesReceived)
                            .append("bytesReceived", reportPayload.bytesReceived);
                },
                payload -> {
                    var reportPayload = ((InboundAudioTrackReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("label", reportPayload.label)
                            .append("trackId", reportPayload.trackId)
                            .append("sfuStreamId", reportPayload.sfuStreamId)
                            .append("sfuSinkId", reportPayload.sfuSinkId)
                            .append("remoteTrackId", reportPayload.remoteTrackId)
                            .append("remoteUserId", reportPayload.remoteUserId)
                            .append("remoteClientId", reportPayload.remoteClientId)
                            .append("remotePeerConnectionId", reportPayload.remotePeerConnectionId)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("ssrc", reportPayload.ssrc)
                            .append("packetsReceived", reportPayload.packetsReceived)
                            .append("packetsLost", reportPayload.packetsLost)
                            .append("jitter", reportPayload.jitter)
                            .append("packetsDiscarded", reportPayload.packetsDiscarded)
                            .append("packetsRepaired", reportPayload.packetsRepaired)
                            .append("burstPacketsLost", reportPayload.burstPacketsLost)
                            .append("burstPacketsDiscarded", reportPayload.burstPacketsDiscarded)
                            .append("burstLossCount", reportPayload.burstLossCount)
                            .append("burstDiscardCount", reportPayload.burstDiscardCount)
                            .append("burstLossRate", reportPayload.burstLossRate)
                            .append("burstDiscardRate", reportPayload.burstDiscardRate)
                            .append("gapLossRate", reportPayload.gapLossRate)
                            .append("gapDiscardRate", reportPayload.gapDiscardRate)
                            .append("voiceActivityFlag", reportPayload.voiceActivityFlag)
                            .append("lastPacketReceivedTimestamp", reportPayload.lastPacketReceivedTimestamp)
                            .append("averageRtcpInterval", reportPayload.averageRtcpInterval)
                            .append("headerBytesReceived", reportPayload.headerBytesReceived)
                            .append("fecPacketsReceived", reportPayload.fecPacketsReceived)
                            .append("fecPacketsDiscarded", reportPayload.fecPacketsDiscarded)
                            .append("bytesReceived", reportPayload.bytesReceived)
                            .append("packetsFailedDecryption", reportPayload.packetsFailedDecryption)
                            .append("packetsDuplicated", reportPayload.packetsDuplicated)
                            .append("perDscpPacketsReceived", reportPayload.perDscpPacketsReceived)
                            .append("nackCount", reportPayload.nackCount)
                            .append("totalProcessingDelay", reportPayload.totalProcessingDelay)
                            .append("estimatedPlayoutTimestamp", reportPayload.estimatedPlayoutTimestamp)
                            .append("jitterBufferDelay", reportPayload.jitterBufferDelay)
                            .append("jitterBufferEmittedCount", reportPayload.jitterBufferEmittedCount)
                            .append("decoderImplementation", reportPayload.decoderImplementation)
                            .append("packetsSent", reportPayload.packetsSent)
                            .append("bytesSent", reportPayload.bytesSent)
                            .append("remoteTimestamp", reportPayload.remoteTimestamp)
                            .append("reportsSent", reportPayload.reportsSent)
                            .append("ended", reportPayload.ended)
                            .append("payloadType", reportPayload.payloadType)
                            .append("mimeType", reportPayload.mimeType)
                            .append("clockRate", reportPayload.clockRate)
                            .append("channels", reportPayload.channels)
                            .append("sdpFmtpLine", reportPayload.sdpFmtpLine);
                },
                payload -> {
                    var reportPayload = ((InboundVideoTrackReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("label", reportPayload.label)
                            .append("trackId", reportPayload.trackId)
                            .append("sfuStreamId", reportPayload.sfuStreamId)
                            .append("sfuSinkId", reportPayload.sfuSinkId)
                            .append("remoteTrackId", reportPayload.remoteTrackId)
                            .append("remoteUserId", reportPayload.remoteUserId)
                            .append("remoteClientId", reportPayload.remoteClientId)
                            .append("remotePeerConnectionId", reportPayload.remotePeerConnectionId)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("ssrc", reportPayload.ssrc)
                            .append("packetsReceived", reportPayload.packetsReceived)
                            .append("packetsLost", reportPayload.packetsLost)
                            .append("jitter", reportPayload.jitter)
                            .append("packetsDiscarded", reportPayload.packetsDiscarded)
                            .append("packetsRepaired", reportPayload.packetsRepaired)
                            .append("burstPacketsLost", reportPayload.burstPacketsLost)
                            .append("burstPacketsDiscarded", reportPayload.burstPacketsDiscarded)
                            .append("burstLossCount", reportPayload.burstLossCount)
                            .append("burstDiscardCount", reportPayload.burstDiscardCount)
                            .append("burstLossRate", reportPayload.burstLossRate)
                            .append("burstDiscardRate", reportPayload.burstDiscardRate)
                            .append("gapLossRate", reportPayload.gapLossRate)
                            .append("gapDiscardRate", reportPayload.gapDiscardRate)
                            .append("framesDropped", reportPayload.framesDropped)
                            .append("partialFramesLost", reportPayload.partialFramesLost)
                            .append("fullFramesLost", reportPayload.fullFramesLost)
                            .append("framesDecoded", reportPayload.framesDecoded)
                            .append("keyFramesDecoded", reportPayload.keyFramesDecoded)
                            .append("frameWidth", reportPayload.frameWidth)
                            .append("frameHeight", reportPayload.frameHeight)
                            .append("frameBitDepth", reportPayload.frameBitDepth)
                            .append("framesPerSecond", reportPayload.framesPerSecond)
                            .append("qpSum", reportPayload.qpSum)
                            .append("totalDecodeTime", reportPayload.totalDecodeTime)
                            .append("totalInterFrameDelay", reportPayload.totalInterFrameDelay)
                            .append("totalSquaredInterFrameDelay", reportPayload.totalSquaredInterFrameDelay)
                            .append("lastPacketReceivedTimestamp", reportPayload.lastPacketReceivedTimestamp)
                            .append("averageRtcpInterval", reportPayload.averageRtcpInterval)
                            .append("headerBytesReceived", reportPayload.headerBytesReceived)
                            .append("fecPacketsReceived", reportPayload.fecPacketsReceived)
                            .append("fecPacketsDiscarded", reportPayload.fecPacketsDiscarded)
                            .append("bytesReceived", reportPayload.bytesReceived)
                            .append("packetsFailedDecryption", reportPayload.packetsFailedDecryption)
                            .append("packetsDuplicated", reportPayload.packetsDuplicated)
                            .append("perDscpPacketsReceived", reportPayload.perDscpPacketsReceived)
                            .append("firCount", reportPayload.firCount)
                            .append("pliCount", reportPayload.pliCount)
                            .append("nackCount", reportPayload.nackCount)
                            .append("sliCount", reportPayload.sliCount)
                            .append("totalProcessingDelay", reportPayload.totalProcessingDelay)
                            .append("estimatedPlayoutTimestamp", reportPayload.estimatedPlayoutTimestamp)
                            .append("jitterBufferDelay", reportPayload.jitterBufferDelay)
                            .append("jitterBufferEmittedCount", reportPayload.jitterBufferEmittedCount)
                            .append("framesReceived", reportPayload.framesReceived)
                            .append("decoderImplementation", reportPayload.decoderImplementation)
                            .append("packetsSent", reportPayload.packetsSent)
                            .append("bytesSent", reportPayload.bytesSent)
                            .append("remoteTimestamp", reportPayload.remoteTimestamp)
                            .append("reportsSent", reportPayload.reportsSent)
                            .append("ended", reportPayload.ended)
                            .append("payloadType", reportPayload.payloadType)
                            .append("mimeType", reportPayload.mimeType)
                            .append("clockRate", reportPayload.clockRate)
                            .append("sdpFmtpLine", reportPayload.sdpFmtpLine);
                },
                payload -> {
                    var reportPayload = ((OutboundAudioTrackReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("label", reportPayload.label)
                            .append("trackId", reportPayload.trackId)
                            .append("sfuStreamId", reportPayload.sfuStreamId)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("ssrc", reportPayload.ssrc)
                            .append("packetsSent", reportPayload.packetsSent)
                            .append("bytesSent", reportPayload.bytesSent)
                            .append("rid", reportPayload.rid)
                            .append("lastPacketSentTimestamp", reportPayload.lastPacketSentTimestamp)
                            .append("headerBytesSent", reportPayload.headerBytesSent)
                            .append("packetsDiscardedOnSend", reportPayload.packetsDiscardedOnSend)
                            .append("bytesDiscardedOnSend", reportPayload.bytesDiscardedOnSend)
                            .append("fecPacketsSent", reportPayload.fecPacketsSent)
                            .append("retransmittedPacketsSent", reportPayload.retransmittedPacketsSent)
                            .append("retransmittedBytesSent", reportPayload.retransmittedBytesSent)
                            .append("targetBitrate", reportPayload.targetBitrate)
                            .append("totalEncodedBytesTarget", reportPayload.totalEncodedBytesTarget)
                            .append("totalSamplesSent", reportPayload.totalSamplesSent)
                            .append("samplesEncodedWithSilk", reportPayload.samplesEncodedWithSilk)
                            .append("samplesEncodedWithCelt", reportPayload.samplesEncodedWithCelt)
                            .append("voiceActivityFlag", reportPayload.voiceActivityFlag)
                            .append("totalPacketSendDelay", reportPayload.totalPacketSendDelay)
                            .append("averageRtcpInterval", reportPayload.averageRtcpInterval)
                            .append("perDscpPacketsSent", reportPayload.perDscpPacketsSent)
                            .append("nackCount", reportPayload.nackCount)
                            .append("encoderImplementation", reportPayload.encoderImplementation)
                            .append("packetsReceived", reportPayload.packetsReceived)
                            .append("packetsLost", reportPayload.packetsLost)
                            .append("jitter", reportPayload.jitter)
                            .append("packetsDiscarded", reportPayload.packetsDiscarded)
                            .append("packetsRepaired", reportPayload.packetsRepaired)
                            .append("burstPacketsLost", reportPayload.burstPacketsLost)
                            .append("burstPacketsDiscarded", reportPayload.burstPacketsDiscarded)
                            .append("burstLossCount", reportPayload.burstLossCount)
                            .append("burstDiscardCount", reportPayload.burstDiscardCount)
                            .append("burstLossRate", reportPayload.burstLossRate)
                            .append("burstDiscardRate", reportPayload.burstDiscardRate)
                            .append("gapLossRate", reportPayload.gapLossRate)
                            .append("gapDiscardRate", reportPayload.gapDiscardRate)
                            .append("roundTripTime", reportPayload.roundTripTime)
                            .append("totalRoundTripTime", reportPayload.totalRoundTripTime)
                            .append("fractionLost", reportPayload.fractionLost)
                            .append("reportsReceived", reportPayload.reportsReceived)
                            .append("roundTripTimeMeasurements", reportPayload.roundTripTimeMeasurements)
                            .append("relayedSource", reportPayload.relayedSource)
                            .append("audioLevel", reportPayload.audioLevel)
                            .append("totalAudioEnergy", reportPayload.totalAudioEnergy)
                            .append("totalSamplesDuration", reportPayload.totalSamplesDuration)
                            .append("echoReturnLoss", reportPayload.echoReturnLoss)
                            .append("echoReturnLossEnhancement", reportPayload.echoReturnLossEnhancement)
                            .append("ended", reportPayload.ended)
                            .append("payloadType", reportPayload.payloadType)
                            .append("mimeType", reportPayload.mimeType)
                            .append("clockRate", reportPayload.clockRate)
                            .append("channels", reportPayload.channels)
                            .append("sdpFmtpLine", reportPayload.sdpFmtpLine);
                },payload -> {
                    var reportPayload = ((OutboundVideoTrackReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("clientId", reportPayload.clientId)
                            .append("userId", reportPayload.userId)
                            .append("peerConnectionId", reportPayload.peerConnectionId)
                            .append("label", reportPayload.label)
                            .append("trackId", reportPayload.trackId)
                            .append("sfuStreamId", reportPayload.sfuStreamId)
                            .append("sampleSeq", reportPayload.sampleSeq)
                            .append("ssrc", reportPayload.ssrc)
                            .append("packetsSent", reportPayload.packetsSent)
                            .append("bytesSent", reportPayload.bytesSent)
                            .append("rid", reportPayload.rid)
                            .append("lastPacketSentTimestamp", reportPayload.lastPacketSentTimestamp)
                            .append("headerBytesSent", reportPayload.headerBytesSent)
                            .append("packetsDiscardedOnSend", reportPayload.packetsDiscardedOnSend)
                            .append("bytesDiscardedOnSend", reportPayload.bytesDiscardedOnSend)
                            .append("fecPacketsSent", reportPayload.fecPacketsSent)
                            .append("retransmittedPacketsSent", reportPayload.retransmittedPacketsSent)
                            .append("retransmittedBytesSent", reportPayload.retransmittedBytesSent)
                            .append("targetBitrate", reportPayload.targetBitrate)
                            .append("totalEncodedBytesTarget", reportPayload.totalEncodedBytesTarget)
                            .append("frameWidth", reportPayload.frameWidth)
                            .append("frameHeight", reportPayload.frameHeight)
                            .append("frameBitDepth", reportPayload.frameBitDepth)
                            .append("framesPerSecond", reportPayload.framesPerSecond)
                            .append("framesSent", reportPayload.framesSent)
                            .append("hugeFramesSent", reportPayload.hugeFramesSent)
                            .append("framesEncoded", reportPayload.framesEncoded)
                            .append("keyFramesEncoded", reportPayload.keyFramesEncoded)
                            .append("framesDiscardedOnSend", reportPayload.framesDiscardedOnSend)
                            .append("qpSum", reportPayload.qpSum)
                            .append("totalEncodeTime", reportPayload.totalEncodeTime)
                            .append("totalPacketSendDelay", reportPayload.totalPacketSendDelay)
                            .append("averageRtcpInterval", reportPayload.averageRtcpInterval)
                            .append("qualityLimitationDurationCPU", reportPayload.qualityLimitationDurationCPU)
                            .append("qualityLimitationDurationNone", reportPayload.qualityLimitationDurationNone)
                            .append("qualityLimitationDurationBandwidth", reportPayload.qualityLimitationDurationBandwidth)
                            .append("qualityLimitationDurationOther", reportPayload.qualityLimitationDurationOther)
                            .append("qualityLimitationReason", reportPayload.qualityLimitationReason)
                            .append("qualityLimitationResolutionChanges", reportPayload.qualityLimitationResolutionChanges)
                            .append("perDscpPacketsSent", reportPayload.perDscpPacketsSent)
                            .append("nackCount", reportPayload.nackCount)
                            .append("firCount", reportPayload.firCount)
                            .append("pliCount", reportPayload.pliCount)
                            .append("sliCount", reportPayload.sliCount)
                            .append("encoderImplementation", reportPayload.encoderImplementation)
                            .append("packetsReceived", reportPayload.packetsReceived)
                            .append("packetsLost", reportPayload.packetsLost)
                            .append("jitter", reportPayload.jitter)
                            .append("packetsDiscarded", reportPayload.packetsDiscarded)
                            .append("packetsRepaired", reportPayload.packetsRepaired)
                            .append("burstPacketsLost", reportPayload.burstPacketsLost)
                            .append("burstPacketsDiscarded", reportPayload.burstPacketsDiscarded)
                            .append("burstLossCount", reportPayload.burstLossCount)
                            .append("burstDiscardCount", reportPayload.burstDiscardCount)
                            .append("burstLossRate", reportPayload.burstLossRate)
                            .append("burstDiscardRate", reportPayload.burstDiscardRate)
                            .append("gapLossRate", reportPayload.gapLossRate)
                            .append("gapDiscardRate", reportPayload.gapDiscardRate)
                            .append("framesDropped", reportPayload.framesDropped)
                            .append("partialFramesLost", reportPayload.partialFramesLost)
                            .append("fullFramesLost", reportPayload.fullFramesLost)
                            .append("roundTripTime", reportPayload.roundTripTime)
                            .append("totalRoundTripTime", reportPayload.totalRoundTripTime)
                            .append("fractionLost", reportPayload.fractionLost)
                            .append("reportsReceived", reportPayload.reportsReceived)
                            .append("roundTripTimeMeasurements", reportPayload.roundTripTimeMeasurements)
                            .append("relayedSource", reportPayload.relayedSource)
                            .append("encodedFrameWidth", reportPayload.encodedFrameWidth)
                            .append("encodedFrameHeight", reportPayload.encodedFrameHeight)
                            .append("encodedFrameBitDepth", reportPayload.encodedFrameBitDepth)
                            .append("encodedFramesPerSecond", reportPayload.encodedFramesPerSecond)
                            .append("ended", reportPayload.ended)
                            .append("payloadType", reportPayload.payloadType)
                            .append("mimeType", reportPayload.mimeType)
                            .append("clockRate", reportPayload.clockRate)
                            .append("channels", reportPayload.channels)
                            .append("sdpFmtpLine", reportPayload.sdpFmtpLine);
                },payload -> {
                    var reportPayload = ((SfuEventReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("sfuId", reportPayload.sfuId)
                            .append("callId", reportPayload.callId)
                            .append("transportId", reportPayload.transportId)
                            .append("mediaStreamId", reportPayload.mediaStreamId)
                            .append("mediaSinkId", reportPayload.mediaSinkId)
                            .append("sctpStreamId", reportPayload.sctpStreamId)
                            .append("rtpPadId", reportPayload.rtpPadId)
                            .append("name", reportPayload.name)
                            .append("message", reportPayload.message)
                            .append("value", reportPayload.value)
                            .append("attachments", reportPayload.attachments);
                },payload -> {
                    var reportPayload = ((SfuMetaReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("sfuId", reportPayload.sfuId)
                            .append("callId", reportPayload.callId)
                            .append("transportId", reportPayload.transportId)
                            .append("mediaStreamId", reportPayload.mediaStreamId)
                            .append("mediaSinkId", reportPayload.mediaSinkId)
                            .append("sctpStreamId", reportPayload.sctpStreamId)
                            .append("rtpPadId", reportPayload.rtpPadId)
                            .append("type", reportPayload.type)
                            .append("payload", reportPayload.payload);
                },payload -> {
                    var reportPayload = ((SfuExtensionReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("sfuId", reportPayload.sfuId)
                            .append("extensionType", reportPayload.extensionType)
                            .append("payload", reportPayload.payload);
                },payload -> {
                    var reportPayload = ((SFUTransportReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("sfuId", reportPayload.sfuId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("transportId", reportPayload.transportId)
                            .append("dtlsState", reportPayload.dtlsState)
                            .append("iceState", reportPayload.iceState)
                            .append("sctpState", reportPayload.sctpState)
                            .append("iceRole", reportPayload.iceRole)
                            .append("localAddress", reportPayload.localAddress)
                            .append("localPort", reportPayload.localPort)
                            .append("protocol", reportPayload.protocol)
                            .append("remoteAddress", reportPayload.remoteAddress)
                            .append("remotePort", reportPayload.remotePort)
                            .append("rtpBytesReceived", reportPayload.rtpBytesReceived)
                            .append("rtpBytesSent", reportPayload.rtpBytesSent)
                            .append("rtpPacketsReceived", reportPayload.rtpPacketsReceived)
                            .append("rtpPacketsSent", reportPayload.rtpPacketsSent)
                            .append("rtpPacketsLost", reportPayload.rtpPacketsLost)
                            .append("rtxBytesReceived", reportPayload.rtxBytesReceived)
                            .append("rtxBytesSent", reportPayload.rtxBytesSent)
                            .append("rtxPacketsReceived", reportPayload.rtxPacketsReceived)
                            .append("rtxPacketsSent", reportPayload.rtxPacketsSent)
                            .append("rtxPacketsLost", reportPayload.rtxPacketsLost)
                            .append("rtxPacketsDiscarded", reportPayload.rtxPacketsDiscarded)
                            .append("sctpBytesReceived", reportPayload.sctpBytesReceived)
                            .append("sctpBytesSent", reportPayload.sctpBytesSent)
                            .append("sctpPacketsReceived", reportPayload.sctpPacketsReceived)
                            .append("sctpPacketsSent", reportPayload.sctpPacketsSent);
                },payload -> {
                    var reportPayload = ((SfuInboundRtpPadReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("sfuId", reportPayload.sfuId)
                            .append("marker", reportPayload.marker)
                            .append("internal", reportPayload.internal)
                            .append("timestamp", reportPayload.timestamp)
                            .append("transportId", reportPayload.transportId)
                            .append("sfuStreamId", reportPayload.sfuStreamId)
                            .append("rtpPadId", reportPayload.rtpPadId)
                            .append("ssrc", reportPayload.ssrc)
                            .append("trackId", reportPayload.trackId)
                            .append("clientId", reportPayload.clientId)
                            .append("callId", reportPayload.callId)
                            .append("mediaType", reportPayload.mediaType)
                            .append("payloadType", reportPayload.payloadType)
                            .append("mimeType", reportPayload.mimeType)
                            .append("clockRate", reportPayload.clockRate)
                            .append("sdpFmtpLine", reportPayload.sdpFmtpLine)
                            .append("rid", reportPayload.rid)
                            .append("rtxSsrc", reportPayload.rtxSsrc)
                            .append("targetBitrate", reportPayload.targetBitrate)
                            .append("voiceActivityFlag", reportPayload.voiceActivityFlag)
                            .append("firCount", reportPayload.firCount)
                            .append("pliCount", reportPayload.pliCount)
                            .append("nackCount", reportPayload.nackCount)
                            .append("sliCount", reportPayload.sliCount)
                            .append("packetsLost", reportPayload.packetsLost)
                            .append("packetsReceived", reportPayload.packetsReceived)
                            .append("packetsDiscarded", reportPayload.packetsDiscarded)
                            .append("packetsRepaired", reportPayload.packetsRepaired)
                            .append("packetsFailedDecryption", reportPayload.packetsFailedDecryption)
                            .append("packetsDuplicated", reportPayload.packetsDuplicated)
                            .append("fecPacketsReceived", reportPayload.fecPacketsReceived)
                            .append("fecPacketsDiscarded", reportPayload.fecPacketsDiscarded)
                            .append("bytesReceived", reportPayload.bytesReceived)
                            .append("rtcpSrReceived", reportPayload.rtcpSrReceived)
                            .append("rtcpRrSent", reportPayload.rtcpRrSent)
                            .append("rtxPacketsReceived", reportPayload.rtxPacketsReceived)
                            .append("rtxPacketsDiscarded", reportPayload.rtxPacketsDiscarded)
                            .append("framesReceived", reportPayload.framesReceived)
                            .append("framesDecoded", reportPayload.framesDecoded)
                            .append("keyFramesDecoded", reportPayload.keyFramesDecoded)
                            .append("fractionLost", reportPayload.fractionLost)
                            .append("jitter", reportPayload.jitter)
                            .append("roundTripTime", reportPayload.roundTripTime);
                },
                payload -> {
                    var reportPayload = ((SfuOutboundRtpPadReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("sfuId", reportPayload.sfuId)
                            .append("marker", reportPayload.marker)
                            .append("internal", reportPayload.internal)
                            .append("timestamp", reportPayload.timestamp)
                            .append("transportId", reportPayload.transportId)
                            .append("sfuStreamId", reportPayload.sfuStreamId)
                            .append("sfuSinkId", reportPayload.sfuSinkId)
                            .append("rtpPadId", reportPayload.rtpPadId)
                            .append("ssrc", reportPayload.ssrc)
                            .append("callId", reportPayload.callId)
                            .append("clientId", reportPayload.clientId)
                            .append("trackId", reportPayload.trackId)
                            .append("mediaType", reportPayload.mediaType)
                            .append("payloadType", reportPayload.payloadType)
                            .append("mimeType", reportPayload.mimeType)
                            .append("clockRate", reportPayload.clockRate)
                            .append("sdpFmtpLine", reportPayload.sdpFmtpLine)
                            .append("rid", reportPayload.rid)
                            .append("rtxSsrc", reportPayload.rtxSsrc)
                            .append("targetBitrate", reportPayload.targetBitrate)
                            .append("voiceActivityFlag", reportPayload.voiceActivityFlag)
                            .append("firCount", reportPayload.firCount)
                            .append("pliCount", reportPayload.pliCount)
                            .append("nackCount", reportPayload.nackCount)
                            .append("sliCount", reportPayload.sliCount)
                            .append("packetsLost", reportPayload.packetsLost)
                            .append("packetsSent", reportPayload.packetsSent)
                            .append("packetsDiscarded", reportPayload.packetsDiscarded)
                            .append("packetsRetransmitted", reportPayload.packetsRetransmitted)
                            .append("packetsFailedEncryption", reportPayload.packetsFailedEncryption)
                            .append("packetsDuplicated", reportPayload.packetsDuplicated)
                            .append("fecPacketsSent", reportPayload.fecPacketsSent)
                            .append("fecPacketsDiscarded", reportPayload.fecPacketsDiscarded)
                            .append("bytesSent", reportPayload.bytesSent)
                            .append("rtcpSrSent", reportPayload.rtcpSrSent)
                            .append("rtcpRrReceived", reportPayload.rtcpRrReceived)
                            .append("rtxPacketsSent", reportPayload.rtxPacketsSent)
                            .append("rtxPacketsDiscarded", reportPayload.rtxPacketsDiscarded)
                            .append("framesSent", reportPayload.framesSent)
                            .append("framesEncoded", reportPayload.framesEncoded)
                            .append("keyFramesEncoded", reportPayload.keyFramesEncoded);
                },
                payload -> {
                    var reportPayload = ((SfuSctpStreamReport) payload);
                    return new Document()
                            .append("serviceId", reportPayload.serviceId)
                            .append("mediaUnitId", reportPayload.mediaUnitId)
                            .append("sfuId", reportPayload.sfuId)
                            .append("marker", reportPayload.marker)
                            .append("timestamp", reportPayload.timestamp)
                            .append("callId", reportPayload.callId)
                            .append("roomId", reportPayload.roomId)
                            .append("transportId", reportPayload.transportId)
                            .append("streamId", reportPayload.streamId)
                            .append("label", reportPayload.label)
                            .append("protocol", reportPayload.protocol)
                            .append("sctpSmoothedRoundTripTime", reportPayload.sctpSmoothedRoundTripTime)
                            .append("sctpCongestionWindow", reportPayload.sctpCongestionWindow)
                            .append("sctpReceiverWindow", reportPayload.sctpReceiverWindow)
                            .append("sctpMtu", reportPayload.sctpMtu)
                            .append("sctpUnackData", reportPayload.sctpUnackData)
                            .append("messageReceived", reportPayload.messageReceived)
                            .append("messageSent", reportPayload.messageSent)
                            .append("bytesReceived", reportPayload.bytesReceived)
                            .append("bytesSent", reportPayload.bytesSent);
                }
        );
        return report -> {
            var payload = payloadMapper.apply(report.payload, report.type);
            return new Document("type", report.type.name())
                    .append("schemaVersion", report.schemaVersion)
                    .append("payload", payload)
                    ;
        };
    }

}
