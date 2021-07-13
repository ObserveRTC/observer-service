package org.observertc.webrtc.observer.sinks.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.sinks.Sink;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        result.put(ReportType.MEDIA_TRACK, MediaTrackReport.class.getSimpleName() + "s");
        result.put(ReportType.OUTBOUND_AUDIO_TRACK, OutboundAudioTrackReport.class.getSimpleName() + "s");
        result.put(ReportType.OUTBOUND_VIDEO_TRACK, OutboundVideoTrackReport.class.getSimpleName() + "s");
        result.put(ReportType.PEER_CONNECTION_DATA_CHANNEL, ClientDataChannelReport.class.getSimpleName() + "s");
        result.put(ReportType.PEER_CONNECTION_TRANPORT, ClientTransportReport.class.getSimpleName() + "s");
        return result;
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

        var documentMappers = DocumentMapper.getDocumentMappers();
        Map<ReportType, String> customCollectionNames = config.collectionNames != null ? config.collectionNames : Collections.EMPTY_MAP;
        getDefaultCollectionNames().forEach((reportType, defaultCollectionName) -> {
            String collectionName = customCollectionNames.getOrDefault(reportType, defaultCollectionName);
            var documentMapper = documentMappers.get(reportType);
            if (Objects.isNull(collectionName) || Objects.isNull(documentMapper)) {
                logger.warn("No Collection name or document mapper to map report type {}", reportType);
                return;
            }
            result.withMapper(reportType, collectionName, documentMapper);
        });

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
