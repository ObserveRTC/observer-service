package org.observertc.webrtc.observer.connectors.sinks.mongo;

import com.mongodb.client.MongoClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.sinks.Sink;

import java.util.List;
import java.util.Properties;

public class MongoSink extends Sink {
    private String topic;
    private Properties properties;
    private boolean tryReconnectOnFailure = false;
    private final MongoClient mongoClient;

    public MongoSink(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }


    @Override
    public void onNext(@NonNull List<EncodedRecord> records) {
        if (records.size() < 1) {
            return;
        }
        try {
            for (EncodedRecord record : records) {

            }
        } catch (Throwable t) {
            logger.error("Unexpected exception", t);
        }
    }


    MongoSink byReconnectOnFailure(boolean value) {
        this.tryReconnectOnFailure = value;
        return this;
    }
}