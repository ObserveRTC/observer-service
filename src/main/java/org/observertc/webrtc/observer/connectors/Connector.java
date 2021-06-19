package org.observertc.webrtc.observer.connectors;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.observer.connectors.sinks.Sink;
import org.observertc.webrtc.observer.connectors.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Connector implements Observer<Report> {

    private String name;
    private final Subject<Report> input = PublishSubject.create();
    private List<Transformation> transformations = new LinkedList<>();
    private Encoder encoder;
    private Sink sink;
    private BufferConfig bufferConfig = null;
    private final Logger logger;
    private RestartPolicy restartPolicy = RestartPolicy.Never;
    private Disposable upstream = null;


    public Connector(String name) {
        this.name = name;
        this.logger = LoggerFactory.getLogger(name);
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        this.upstream = d;
        Observable<Report> observableReport = this.input;

        for (Transformation transformation : this.transformations) {
            observableReport = observableReport.lift(transformation).share();
        }

        var observableRecord = observableReport.map(this.encoder).share();

        Observable<List<EncodedRecord>> observableRecords;
        if (1 < this.bufferConfig.maxItems) {
            if (this.bufferConfig.maxWaitingTimeInS < 1) {
                observableRecords = observableRecord.buffer(this.bufferConfig.maxItems).share();
            } else {
                observableRecords = observableRecord.buffer(this.bufferConfig.maxWaitingTimeInS, TimeUnit.SECONDS, this.bufferConfig.maxItems).share();
            }
        } else {
            observableRecords = observableRecord.map(List::of);
        }


        observableRecords
                .subscribe(this.sink);
    }

    public void unSubscribe() {
        this.input.onComplete();
        this.sink.onComplete();
        if (Objects.nonNull(this.upstream) && !this.upstream.isDisposed()) {
            this.upstream.dispose();
        }
    }

    @Override
    public void onNext(@NonNull Report report) {
        try {
            this.input.onNext(report);
        } catch (Throwable t) {
            logger.error("Unexpected error occurred during streaming process", t);
            if (Objects.isNull(this.restartPolicy) || this.restartPolicy.equals(RestartPolicy.Never)) {
                throw new RuntimeException(t);
            }
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        this.unSubscribe();
        logger.error("Unexpected error occurred.", e);
    }

    @Override
    public void onComplete() {
        this.unSubscribe();
        logger.info("The Connector is finished");
    }

    public String getName() {
        if (Objects.isNull(this.name)) {
            return "Unkown pipeline";
        }
        return this.name;
    }

    Connector withRestartPolicy(RestartPolicy restartPolicy) {
        this.restartPolicy = restartPolicy;
        return this;
    }

    Connector withBuffer(BufferConfig bufferConfig) {
        this.bufferConfig = bufferConfig;
        return this;
    }

    Connector withTransformation(Transformation transformation) {
        this.transformations.add(transformation);
        return this;
    }

    Connector withEncoder(Encoder encoder) {
        this.encoder = encoder;
        return this;
    }

    Connector withSink(Sink sink) {
        if (Objects.nonNull(this.sink)) {
            throw new IllegalStateException(this.getName() + ": cannot set the source for a pipeline twice");
        }
        this.sink = sink
                .withLogger(logger)
        ;
        return this;
    }

    private List<EncodedRecord> mapper(List<Report> reports) {
        if (Objects.isNull(reports)) {
            return null;
        }
        List<EncodedRecord> result = new LinkedList<>();
        for (Report report : reports) {
            try {
                EncodedRecord record = this.encoder.apply(report);
                if (Objects.isNull(record)) {
                    continue;
                }
                result.add(record);
            } catch (Throwable t) {
                logger.warn("Encoding failure", t);
            }
        }
        return result;
    }

}
