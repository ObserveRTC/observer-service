package org.observertc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.RxObserverBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class ObservableCollectorTest {

    @Test
    public void shouldCollect_1() throws Throwable {
        AtomicBoolean done = new AtomicBoolean(false);
        var collector = ObservableCollector.<Integer>builder().withMaxItems(2)
                .build();

        collector.observableEmittedItems().subscribe(items -> {
            Assertions.assertEquals(1, items.get(0));
            Assertions.assertEquals(2, items.get(1));
            Assertions.assertEquals(2, items.size());
            done.set(true);
        });

        collector.add(1, 2);
        collector.add(3);
        Assertions.assertTrue(done.get());
    }

    @Test
    public void shouldBatchCollect_1() throws Throwable {
        AtomicBoolean done = new AtomicBoolean(false);
        var collector = ObservableCollector.<Integer>builder().withMaxItems(2).build();

        collector.observableEmittedItems().subscribe(items -> {
            Assertions.assertEquals(1, items.get(0));
            Assertions.assertEquals(2, items.get(1));
            Assertions.assertEquals(3, items.get(2));
            Assertions.assertEquals(3, items.size());
            done.set(true);
        });

        collector.addAll(List.of(1,2,3));
        Assertions.assertTrue(done.get());
    }

    @Test
    public void shouldCollect_2() throws Throwable {
        AtomicBoolean done = new AtomicBoolean(false);
        var collector = ObservableCollector.<Integer>builder().withMaxTimeInMs(200).build();

        collector.observableEmittedItems().subscribe(items -> {
            Assertions.assertEquals(1, items.size());
            done.set(true);
        });

        collector.add(1);
        new Sleeper(() -> 500).run();
        Assertions.assertTrue(done.get());
    }

    @Test
    public void shouldCollectOnce_1() throws Throwable {
        AtomicInteger executed = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder().withMaxItems(2).withMaxTimeInMs(200).build();

        collector.observableEmittedItems().subscribe(items -> {
            executed.incrementAndGet();
        });

        collector.add(1, 2);
        new Sleeper(() -> 500).run();
        Assertions.assertEquals(1, executed.get());
    }

    @Test
    public void shouldCollectOnce_2() throws Throwable {
        AtomicInteger executed = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder().withMaxItems(2).withMaxTimeInMs(200).build();

        collector.observableEmittedItems().subscribe(items -> {
            executed.incrementAndGet();
        });

        collector.add(1);
        new Sleeper(() -> 500).run();
        collector.add(2);
        Assertions.assertEquals(1, executed.get());
    }

    @Test
    public void shouldCollectAndRestart_1() throws Throwable {
        AtomicInteger expected = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder().withMaxItems(1).build();

        collector.observableEmittedItems().subscribe(items -> {
            expected.incrementAndGet();
            Assertions.assertEquals(1, items.size());
            Assertions.assertEquals(expected.get(), items.get(0));
        });

        collector.add(1, 2, 3, 4, 5);
        Assertions.assertEquals(5, expected.get());
    }



    @Test
    public void shouldCollectAndRestart_2() throws Throwable {
        AtomicInteger expected = new AtomicInteger();
        var collector = ObservableCollector.<Integer>builder().withMaxTimeInMs(200).build();

        collector.observableEmittedItems().subscribe(items -> {
            Assertions.assertEquals(2, items.size());
            expected.incrementAndGet();
        });

        collector.add(1, 2);
        new Sleeper(() -> 500).run();
        collector.add(3, 4);
        new Sleeper(() -> 500).run();
        Assertions.assertEquals(2, expected.get());
    }



    @Test
    public void shouldCollectAndRestart_3() throws Throwable {
        AtomicInteger executed = new AtomicInteger();
        var collector = ObservableCollector.<Integer>builder().withMaxTimeInMs(200).withMaxItems(2).build();

        collector.observableEmittedItems().subscribe(items -> {
            executed.incrementAndGet();
        });

        collector.add(1, 2, 3, 4);
        new Sleeper(() -> 500).run();
        collector.add(5);
        new Sleeper(() -> 500).run();
        collector.add(6);
        Assertions.assertEquals(3, executed.get());
    }

    @Test
    public void shouldEmitOnFlush_1() throws Throwable {
        AtomicInteger executed = new AtomicInteger();
        var collector = ObservableCollector.<Integer>builder().withMaxItems(2).build();

        collector.observableEmittedItems().subscribe(items -> {
            executed.incrementAndGet();
        });

        collector.add(1);
        collector.flush();
        Assertions.assertEquals(1, executed.get());
    }

    @Test
    void shouldInvokeAllSubscribers() {
        var receivedItems = new AtomicInteger(0);
        var collector = ObservableCollector.<Integer>builder()
                .withMaxItems(2)
                .build();
        collector.observableEmittedItems().subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> receivedItems.addAndGet(items.size()))
                .build());
        collector.observableEmittedItems().subscribe(new RxObserverBuilder<List<Integer>>()
                .onNext(items -> receivedItems.addAndGet(items.size()))
                .build());

        collector.addAll(List.of(1,2));
        Assertions.assertEquals(4, receivedItems.get());
    }

    @Test
    void shouldAcceptMultipleSources() {
        var receivedItems = new AtomicInteger(0);
        var source_1 = ObservableCollector.<Integer>builder().withMaxItems(1).build();
        var source_2 = ObservableCollector.<Integer>builder().withMaxItems(2).build();
        var sink = ObservableCollector.<List<Integer>>builder().withMaxItems(2).build();

        source_1.observableEmittedItems().subscribe(sink::add);
        source_2.observableEmittedItems().subscribe(sink::add);
        sink.observableEmittedItems().subscribe(itemsOfItems -> {
            itemsOfItems.stream().forEach(items -> {
                receivedItems.addAndGet(items.size());
            });
        });

        source_1.addAll(List.of(1, 2));
        source_2.addAll(List.of(3, 4));
        Assertions.assertEquals(4, receivedItems.get());
    }


    @Test
    public void shoulNotBuiltWithoutSetup() throws Throwable {
        Assertions.assertThrows(Exception.class, () -> {
            ObservableCollector.<Integer>builder().build();
        });
    }
}