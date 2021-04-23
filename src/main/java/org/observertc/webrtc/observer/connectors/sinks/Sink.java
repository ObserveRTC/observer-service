package org.observertc.webrtc.observer.connectors.sinks;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public abstract class Sink implements Observer<List<EncodedRecord>> {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(Sink.class);
    private Disposable upstream;
    protected Logger logger = DEFAULT_LOGGER;

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        this.upstream = d;
    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (Objects.nonNull(this.upstream)) {
            if (!upstream.isDisposed()) {
                this.upstream.dispose();
            }
        }
        logger.warn("Error occurred in pipeline ", e);
    }

    @Override
    public void onComplete() {
        if (Objects.nonNull(this.upstream)) {
            if (!upstream.isDisposed()) {
                this.upstream.dispose();
            }
        }
        logger.info("Pipeline is completed");
    }

    public Sink withLogger(Logger logger) {
        this.logger.info("Default logger for {} is switched to {}", this.getClass().getSimpleName(), logger.getName());
        this.logger = logger;
        return this;
    }
}
