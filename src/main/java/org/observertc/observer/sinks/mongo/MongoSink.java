package org.observertc.observer.sinks.mongo;

import com.mongodb.client.MongoClient;
import io.reactivex.rxjava3.annotations.NonNull;
import org.bson.Document;
import org.observertc.observer.common.Utils;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.Sink;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MongoSink extends Sink {
    private int consecutiveEmptyLists = 0;
    private final boolean logSummary;
    private final Supplier<MongoClient> clientProvider;
    private final String databaseName;
    private final Function<Report, Document> documentMapper;
    private final Function<List<Report>, Map<String, List<Report>>> documentSorter;


    public MongoSink(boolean logSummary, Supplier<MongoClient> clientProvider, String databaseName, Function<Report, Document> documentMapper, Function<List<Report>, Map<String, List<Report>>> documentSorter) {
        this.logSummary = logSummary;
        this.clientProvider = clientProvider;
        this.documentMapper = documentMapper;
        this.documentSorter = documentSorter;
        this.databaseName = databaseName;
    }


    @Override
    public void process(@NonNull List<Report> reports) {
        if (reports.size() < 1) {
            if (3 < ++this.consecutiveEmptyLists) {
                // keep the connection alive
                this.clientProvider.get().listDatabaseNames();
            }
            return;
        }
        this.consecutiveEmptyLists = 0;

        try {
            var client = this.clientProvider.get();
            var database = client.getDatabase(this.databaseName);
            var collectionReports = this.documentSorter.apply(reports);
            for (var entry : collectionReports.entrySet()) {
                var documents = entry.getValue().stream().map(this.documentMapper).collect(Collectors.toList());
                var collectionName = entry.getKey();
                var collection = database.getCollection(collectionName);
                var inserted = collection.insertMany(documents);
                if (this.logSummary) {
                    logger.info("Inserted {} number of entries into {} collection",
                            Utils.anyNull(inserted, inserted.getInsertedIds()) ? 0 : inserted.getInsertedIds().size(),
                            collectionName
                    );
                }
            }
        } catch (Throwable t) {
            logger.error("Unexpected exception while inserting documents", t);
        }
    }

    @Override
    public void close() {
        try {
            var mongoClient = this.clientProvider.get();
            if (Objects.nonNull(mongoClient)) {
                mongoClient.close();
            }
        } finally {
            super.close();
        }
    }
}
