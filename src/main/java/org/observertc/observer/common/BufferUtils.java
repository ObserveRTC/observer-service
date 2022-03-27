package org.observertc.observer.common;

import io.reactivex.rxjava3.core.Observable;
import org.observertc.observer.configs.ObserverConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BufferUtils {

    public static<T> Observable<List<T>> wrapObservable(Observable<T> source, ObserverConfig.CollectorConfig config) {
        var maxItems = config.maxItems;
        var maxTimeInMs = config.maxTimeInMs;
        if (maxItems < 1 && maxTimeInMs < 1) {
            return source.map(List::of);
        }
        if (maxItems < 1) {
            return source.buffer(maxTimeInMs, TimeUnit.MILLISECONDS);
        }
        if (maxTimeInMs < 1) {
            return source.buffer(maxItems);
        }
        return source.buffer(maxTimeInMs, TimeUnit.MILLISECONDS, maxItems);
    }
}
