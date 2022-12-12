package org.observertc.observer.common;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class ObservableState<T extends Enum> {

    private static final Logger logger = LoggerFactory.getLogger(ObservableState.class);

    private final AtomicReference<T> actual;
    private final Subject<StateChangeEvent<T>> stateChanges = PublishSubject.<StateChangeEvent<T>>create().toSerialized();

    public ObservableState() {
        this(null);
    }

    public ObservableState(T initialValue) {
        this.actual = new AtomicReference(initialValue);
    }

    public boolean compareAndSetState(T expectedState, T newState) {
        if (!this.actual.compareAndSet(expectedState, newState)) {
            return false;
        }
        if (expectedState == null && newState == null) return true;
        if (expectedState != null && newState != null && expectedState.equals(newState)) return true;
        this.stateChanges.onNext(new StateChangeEvent<>(
                expectedState,
                newState
        ));
        return true;
    }

    public T get() {
        return this.actual.get();
    }

    public void setState(T newState) {
        if (newState == null) {
            logger.warn("State to change cannot be null");
            return;
        }
        var prevState = this.actual.getAndSet(newState);
        if (prevState != null && !prevState.equals(newState)) {
            this.stateChanges.onNext(new StateChangeEvent<T>(
                    prevState,
                    newState
            ));
        }
    }

    public Observable<StateChangeEvent<T>> stateChanges() {
        return this.stateChanges;
    }


    public record StateChangeEvent<U>(
            U prevState,
            U actualState
    ) {

    }
}
