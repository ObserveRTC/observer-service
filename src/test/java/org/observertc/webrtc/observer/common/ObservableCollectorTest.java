package org.observertc.webrtc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.RxObserverBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@MicronautTest
class ObservableCollectorTest {


    @Test
    void shouldCollectMaxItemsWithSubscriber() {
        AtomicBoolean executed = new AtomicBoolean(false);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(2)
                .build();
        collector.subscribe(items -> {
            Assertions.assertEquals(2, items.size());
            executed.set(true);
        });

        collector.addAll(List.of(1,2));
        Assertions.assertTrue(executed.get());
    }

    @Test
    void shouldCollectTimeoutInMsWithSubscriber() {
        AtomicBoolean executed = new AtomicBoolean(false);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxTimeInMs(200)
                .build();
        collector.subscribe(items -> {
            Assertions.assertEquals(2, items.size());
            executed.set(true);
        });

        collector.addAll(List.of(1,2));
        new Sleeper(() -> 500).run();
        collector.add(1);
        Assertions.assertTrue(executed.get());
    }

    @Test
    void shouldCompleteAllSubscribers() {
        var completed = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(1)
                .build();
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onCompleted(completed::incrementAndGet)
                .build());
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onCompleted(completed::incrementAndGet)
                .build());

        collector.onComplete();
        Assertions.assertEquals(2, completed.get());
    }

    @Test
    void shouldInvokeAllSubscribers() {
        var receivedItems = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(2)
                .build();
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> receivedItems.addAndGet(items.size()))
                .build());
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> receivedItems.addAndGet(items.size()))
                .build());

        collector.addAll(List.of(1,2));
        Assertions.assertEquals(4, receivedItems.get());
    }

    @Test
    void shouldFlushOnComplete() {
        var receivedItems = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(3)
                .build();
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> receivedItems.addAndGet(items.size()))
                .build());
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> receivedItems.addAndGet(items.size()))
                .build());

        collector.addAll(List.of(1,2));
        collector.onComplete();
        Assertions.assertEquals(4, receivedItems.get());
    }

    @Test
    void shouldAcceptMultipleSources() {
        var receivedItems = new AtomicInteger(0);
        var source_1 = ObservableCollector.<Integer>builder().withMaxItems(1).build();
        var source_2 = ObservableCollector.<Integer>builder().withMaxItems(2).build();
        var sink = ObservableCollector.<List<Integer>>builder().withMaxItems(2).build();

        source_1.subscribe(sink);
        source_2.subscribe(sink);
        sink.subscribe(itemsOfItems -> {
            itemsOfItems.stream().forEach(items -> {
                receivedItems.addAndGet(items.size());
            });
        });

        source_1.addAll(List.of(1, 2));
        source_2.addAll(List.of(3, 4));
        Assertions.assertEquals(4, receivedItems.get());
    }

    @Test
    void shouldFlushForward_1() {
        var receivedItems = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(1)
                .build();

        collector.buffer(2).subscribe(items -> {
            receivedItems.addAndGet(items.size());
        });

        collector.add(1);
        collector.onComplete();
        Assertions.assertEquals(1, receivedItems.get());
    }

    @Test
    void shouldFlushForward_2() {
        var receivedItems = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(1)
                .build();

        collector.buffer(2).subscribe(items -> {
            receivedItems.addAndGet(items.size());
        });

        collector.add(1);
        collector.onError(new RuntimeException());
        Assertions.assertEquals(1, receivedItems.get());
    }

    @Test
    void shouldCloseOnlyErroredSubscriber() {
        var receivedItems = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(2)
                .build();

        collector.subscribe(items -> {
            throw new RuntimeException();
        });
        collector.subscribe(items -> {
            receivedItems.addAndGet(items.size());
        });

        collector.addAll(List.of(1,2));
        Assertions.assertEquals(2, receivedItems.get());
    }

    @Test
    void shouldCallOnErrorIfExceptionIsThrown() {
        var onErrorIsCalled = new AtomicBoolean(false);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(2)
                .withResilientOutput(false)
                .build();
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> { throw new RuntimeException();})
                .onError(t -> onErrorIsCalled.set(true))
                .build());

        collector.addAll(List.of(1,2));
        Assertions.assertTrue(onErrorIsCalled.get());
    }

    @Test
    void shouldNotCallOnErrorIfExceptionIsThrownWhenOutputsAreResilient() {
        var onErrorIsCalled = new AtomicBoolean(false);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(2)
                .withResilientOutput(true)
                .build();
        collector.subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> { throw new RuntimeException();})
                .onError(t -> onErrorIsCalled.set(true))
                .build());

        collector.addAll(List.of(1,2));
        Assertions.assertFalse(onErrorIsCalled.get());
    }


}