package org.observertc.webrtc.observer.sinks.mongo;

import com.mongodb.client.MongoClient;
import io.reactivex.rxjava3.annotations.NonNull;
import org.bson.Document;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.observer.sinks.Sink;
import org.observertc.webrtc.schemas.reports.ReportType;

import java.util.*;

public class MongoSink extends Sink {
    private String database;
    private final MongoClient mongoClient;
    private int consecutiveEmptyLists = 0;
    private Map<ReportType, DocumentMapper> mappers = new HashMap<>();
    private Map<ReportType, String> collectionNames = new HashMap<>();
    private boolean logSummary = false;


    public MongoSink(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }


    @Override
    public void accept(@NonNull OutboundReports outboundReports) {
        if (outboundReports.getReportsNum() < 1) {
            if (3 < ++this.consecutiveEmptyLists) {
                // keep the connection alive
                this.mongoClient.listDatabaseNames();
            }
            return;
        } else {
            this.consecutiveEmptyLists = 0;
        }

        Map<ReportType, List<Document>> documents = new HashMap<>();
        try {
            for (OutboundReport outboundReport : outboundReports) {
                ReportType reportType = outboundReport.getType();
                var mapper = this.mappers.get(reportType);
                if (Objects.isNull(mapper)) {
                    logger.warn("No mapper for report type, report cannot be decoded and mapped to Document, therefore it is dropped here", reportType);
                    continue;
                }
                Document document = mapper.apply(outboundReport);
                List<Document> typeDocs = documents.get(reportType);
                if (Objects.isNull(typeDocs)) {
                    typeDocs = new LinkedList<>();
                    documents.put(reportType, typeDocs);
                }
                typeDocs.add(document);
            }
        } catch (Throwable t) {
            logger.error("Unexpected exception while collecting documents", t);
        }

        try {
            for (var entry : documents.entrySet()) {
                ReportType reportType = entry.getKey();
                List<Document> docs = entry.getValue();
                var db = this.mongoClient.getDatabase(this.database);
                var collectionName = this.collectionNames.getOrDefault(reportType, reportType.name());
                var collection = db.getCollection(collectionName);
                var inserted = collection.insertMany(docs);
                if (this.logSummary) {
                    logger.info("Inserted {} number of entries into {} collection",
                            Utils.anyNull(inserted, inserted.getInsertedIds()) ? 0 : inserted.getInsertedIds().size(),
                            collectionName
                    );
                }
            }
        } catch (Throwable t) {
            logger.error("Unexpected exception while inserting documents to the database", t);
        }
    }

    @Override
    public void close() {
        try {
            if (Objects.nonNull(this.mongoClient)) {
                this.mongoClient.close();
            }
        } finally {

        }
    }


    MongoSink withDatabase(String database) {
        this.database = database;
        return this;
    }

    MongoSink withLogSummary(boolean value) {
        this.logSummary = value;
        return this;
    }

    MongoSink withMapper(ReportType reportType, String collectionName, DocumentMapper documentMapper) {
        this.mappers.put(reportType, documentMapper);
        this.collectionNames.put(reportType, collectionName);
        return this;
    }
}
