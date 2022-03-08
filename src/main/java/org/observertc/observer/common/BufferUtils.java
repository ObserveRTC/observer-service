package org.observertc.observer.common;

import io.reactivex.rxjava3.core.Observable;
import org.observertc.observer.configs.ObserverConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BufferUtils {

    public static<T> Observable<List<T>> wrapObservable(Observable<T> source, ObserverConfig.CollectorConfig config) {
        var maxItems = config.maxItems;
        var maxTimeInS = config.maxTimeInS;
        if (maxItems < 1 && maxTimeInS < 1) {
            return source.map(List::of);
        }
        if (maxItems < 1) {
            return source.buffer(maxTimeInS, TimeUnit.SECONDS);
        }
        if (maxTimeInS < 1) {
            return source.buffer(maxItems);
        }
        return source.buffer(maxTimeInS, TimeUnit.SECONDS, maxItems);
    }
}
