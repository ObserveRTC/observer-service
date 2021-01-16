package org.observertc.webrtc.observer.connector.transformations;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.ProtocolViolationException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public abstract class Transformation implements ObservableOperator<Report, Report> {

    private static final Logger logger = LoggerFactory.getLogger(Transformation.class);

    @Override
    public @NonNull Observer<? super Report> apply(@NonNull Observer<? super Report> observer) throws Throwable {
        final Observer<? super Report> downstream = observer;

        return new Observer<Report>() {
            private boolean done = false;
            private Disposable upstream = null;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                if (Objects.nonNull(upstream)) {
                    RxJavaPlugins.onError(new ProtocolViolationException("Cannot have two upstream component"));
                    return;
                }
                if (Objects.isNull(d)) {
                    logger.warn("Upstream is null {}", d);
                    return;
                }
                this.upstream = d;
            }

            @Override
            public void onNext(@NonNull Report report) {
                if (done) {
                    return;
                }
                Optional<Report> reportHolder;
                try {
                    reportHolder = Transformation.this.transform(report);
                } catch (Throwable t) {
                    done = true;
                    this.onError(t);
                    return;
                }

                if (!reportHolder.isPresent()) {
                    return;
                }
                downstream.onNext(reportHolder.get());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                if (done) {
                    RxJavaPlugins.onError(e);
                    return;
                }

                done = true;
                upstream.dispose();
                downstream.onError(e);
            }

            @Override
            public void onComplete() {
                if (!done) {
                    done = true;
                    upstream.dispose();
                    downstream.onComplete();
                }
            }
        };
    }

    protected abstract Optional<Report> transform(Report report) throws Throwable;
}
