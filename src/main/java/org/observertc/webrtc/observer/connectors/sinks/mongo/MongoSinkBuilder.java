package org.observertc.webrtc.observer.connectors.sinks.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.connectors.sinks.Sink;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Prototype
public class MongoSinkBuilder extends AbstractBuilder implements Builder<Sink> {
    private final static Logger logger = LoggerFactory.getLogger(MongoSinkBuilder.class);

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        MongoClient mongoClient;
        try {
            mongoClient = MongoClients.create(config.URI);
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

        result.withCollectionNames(this.getDefaultCollectionNames());
        if (Objects.nonNull(config.collectionNames)) {
            result.withCollectionNames(config.collectionNames);
        }
        return result.withLogSummary(config.printSummary);
    }

    private Map<ReportType, String> getDefaultCollectionNames() {
        final Map<ReportType, String> result = new HashMap<>();
        result.put(ReportType.INITIATED_CALL, InitiatedCall.class.getSimpleName() + "s");
        result.put(ReportType.FINISHED_CALL, FinishedCall.class.getSimpleName() + "s");
        result.put(ReportType.JOINED_PEER_CONNECTION, JoinedPeerConnection.class.getSimpleName() + "s");
        result.put(ReportType.DETACHED_PEER_CONNECTION, DetachedPeerConnection.class.getSimpleName() + "s");
        result.put(ReportType.INBOUND_RTP, InboundRTP.class.getSimpleName() + "s");
        result.put(ReportType.REMOTE_INBOUND_RTP, RemoteInboundRTP.class.getSimpleName() + "s");
        result.put(ReportType.OUTBOUND_RTP, OutboundRTP.class.getSimpleName() + "s");
        result.put(ReportType.ICE_CANDIDATE_PAIR, ICECandidatePair.class.getSimpleName() + "s");
        result.put(ReportType.ICE_LOCAL_CANDIDATE, ICELocalCandidate.class.getSimpleName() + "s");
        result.put(ReportType.ICE_REMOTE_CANDIDATE, ICERemoteCandidate.class.getSimpleName() + "s");
        result.put(ReportType.TRACK, Track.class.getSimpleName() + "s");
        result.put(ReportType.MEDIA_SOURCE, MediaSource.class.getSimpleName() + "s");
        result.put(ReportType.USER_MEDIA_ERROR, UserMediaError.class.getSimpleName() + "s");
        result.put(ReportType.MEDIA_DEVICE, MediaDevice.class.getSimpleName() + "s");
        result.put(ReportType.CLIENT_DETAILS, ClientDetails.class.getSimpleName() + "s");
        result.put(ReportType.EXTENSION, ExtensionReport.class.getSimpleName() + "s");
        result.put(ReportType.OBSERVER_EVENT, ObserverEventReport.class.getSimpleName() + "s");
        return result;
    }

    public static class Config {

        @NotNull
        public String URI;

        @NotNull
        public String database;

        public Map<ReportType, String> collectionNames = null;

        public boolean printSummary = false;
    }
}
