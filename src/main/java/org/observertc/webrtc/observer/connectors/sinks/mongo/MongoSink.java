package org.observertc.webrtc.observer.connectors.sinks.mongo;

import com.mongodb.client.MongoClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import org.bson.Document;
import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.sinks.Sink;

import java.util.*;

public class MongoSink extends Sink {
    private String database;
    private final MongoClient mongoClient;
    private int consecutiveEmptyLists = 0;
    private Map<ReportType, String> collectionNames = new HashMap<>();
    private boolean logSummary = false;

    public MongoSink(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }


    @Override
    public void onNext(@NonNull List<EncodedRecord> records) {
        if (records.size() < 1) {
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
            for (EncodedRecord record : records) {
                ReportType reportType = record.getReportType();
                Document document = record.getMessage();
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


    MongoSink withDatabase(String database) {
        this.database = database;
        return this;
    }

    MongoSink withCollectionNames(Map<ReportType, String> collectionNames) {
        this.collectionNames.putAll(collectionNames);
        return this;
    }

    MongoSink withLogSummary(boolean value) {
        this.logSummary = value;
        return this;
    }
}