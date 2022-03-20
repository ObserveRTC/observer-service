package org.observertc.observer.sinks.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.sinks.Sink;
import org.observertc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

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
        MongoClient mongoClient;
        try {
            mongoClient = MongoClients.create(config.uri);
        } catch (Throwable t) {
            logger.error("cannot make mongo client", t);
            return null;
        }

        MongoSink result;
        try {
            result = new MongoSink(mongoClient).withDatabase(config.database);
        } catch (Throwable t) {
            logger.error("cannot make mongo sink", t);
            return null;
        }

        return result.withLogSummary(config.printSummary);
    }

    public static class Config {

        @NotNull
        public String uri;

        @NotNull
        public String database;

        public Map<ReportType, String> collectionNames = null;

        public boolean printSummary = false;
    }
}
