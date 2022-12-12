package org.observertc.observer.sinks.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.micronaut.context.annotation.Prototype;
import org.bson.Document;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configs.InvalidConfigurationException;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.reports.ReportTypeVisitors;
import org.observertc.observer.sinks.ISinkBuilder;
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
public class MongoSinkBuilder extends AbstractBuilder implements ISinkBuilder {
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

    private Function<Report, Document> makeReportMapper() {
        var payloadMapper = new ReportMapper();
        return report -> {
            var payload = payloadMapper.apply(report.payload, report.type);
            return new Document("type", report.type.name())
                    .append("schemaVersion", report.schemaVersion)
                    .append("payload", payload)
                    ;
        };
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
}
