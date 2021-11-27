package org.observertc.webrtc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class PassiveCollectorTest {

    @Test
    public void checkBatchCollector_1() throws Throwable {
        var expectedItem = new AtomicInteger(0);
        var queue = new LinkedBlockingQueue<Integer>();
        queue.addAll(List.of(1,2,3,4));
        var myList = new LinkedList<>();
        queue.drainTo(myList);
        myList.stream().collect(BatchCollector.builder().withBatchSize(2).withConsumer(new Consumer<List>() {
            @Override
            public void accept(List list) {
                Assertions.assertEquals(2, list.size());
                list.forEach(item -> Assertions.assertEquals(item, expectedItem.incrementAndGet()));
            }
        }).build());

        Assertions.assertEquals(4, expectedItem.get());
        Assertions.assertTrue(queue.isEmpty());
    }

    @Test
    public void shouldCollect_1() throws Throwable {
        var collector = PassiveCollector.<Integer>builder().withMaxItems(2)
                .withListener(items -> {
                    Assertions.assertEquals(1, items.get(0));
                    Assertions.assertEquals(2, items.get(1));
                    Assertions.assertEquals(2, items.size());
                })
                .build();


        collector.add(1, 2);
        collector.add(3);
    }

    @Test
    public void shouldBatchCollect_1() throws Throwable {
        var collector = PassiveCollector.<Integer>builder().withMaxItems(2)
                .withListener(items -> {
                    Assertions.assertEquals(1, items.get(0));
                    Assertions.assertEquals(2, items.get(1));
                    Assertions.assertEquals(3, items.get(2));
                    Assertions.assertEquals(3, items.size());
                })
                .build();

        collector.addAll(List.of(1,2,3));
    }

    @Test
    public void shouldCollect_2() throws Throwable {
        AtomicBoolean done = new AtomicBoolean(false);
        var collector = PassiveCollector.<Integer>builder().withMaxTimeInMs(200)
                .withListener(items -> {
                    Assertions.assertEquals(1, items.size());
                    done.set(true);
                })
                .build();

        collector.add(1);
        new Sleeper(() -> 500).run();
        collector.add(2);
        Assertions.assertTrue(done.get());
    }

    @Test
    public void shouldBatchCollect_2() throws Throwable {
        AtomicBoolean done = new AtomicBoolean(false);
        var collector = PassiveCollector.<Integer>builder().withMaxTimeInMs(200)
                .withListener(items -> {
                    Assertions.assertEquals(1, items.size());
                    done.set(true);
                })
                .build();

        collector.addAll(List.of(1));
        new Sleeper(() -> 500).run();
        collector.addAll(List.of(2));
        Assertions.assertTrue(done.get());
    }

    @Test
    public void shouldCollectAndRestart_1() throws Throwable {
        AtomicInteger executed = new AtomicInteger(0);
        var collector = PassiveCollector.<Integer>builder().withMaxItems(1)
                .withListener(items -> {
                    executed.incrementAndGet();
                    Assertions.assertEquals(1, items.size());
                    Assertions.assertEquals(executed.get(), items.get(0));
                })
                .build();

        collector.add(1, 2, 3, 4, 5);
        Assertions.assertEquals(5, executed.get());
    }

    @Test
    public void shouldCollectAndRestart_2() throws Throwable {
        AtomicInteger executed = new AtomicInteger();
        var collector = PassiveCollector.<Integer>builder().withMaxTimeInMs(200)
                .withListener(items -> {
                    Assertions.assertEquals(2, items.size());
                    executed.incrementAndGet();
                })
                .build();


        collector.add(1, 2);
        new Sleeper(() -> 500).run();
        collector.add(3, 4);
        new Sleeper(() -> 500).run();
        collector.add(1);
        Assertions.assertEquals(2, executed.get());
    }

    @Test
    public void shouldCollectAndRestart_3() throws Throwable {
        AtomicInteger executed = new AtomicInteger();
        var collector = PassiveCollector.<Integer>builder().withMaxTimeInMs(200).withMaxItems(2)
                .withListener(items -> {
                    executed.incrementAndGet();
                })
                .build();


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
        var collector = PassiveCollector.<Integer>builder().withMaxItems(2)
                .withListener(items -> {
                    executed.incrementAndGet();
                })
                .build();


        collector.add(1);
        collector.flush();
        Assertions.assertEquals(1, executed.get());
    }

    @Test
    public void shouldEmitMutableResults() throws Throwable {
        AtomicInteger executed = new AtomicInteger();
        var collector = PassiveCollector.<Integer>builder().withMaxItems(1)
                .withMutableResults(true)
                .withListener(items -> {
                    executed.incrementAndGet();
                    items.add(1);
                })
                .build();

        collector.add(1);
        collector.flush();
        Assertions.assertEquals(1, executed.get());
    }

    @Test
    public void shouldEmitImmutableResults() throws Throwable {
        AtomicInteger executed = new AtomicInteger();
        var collector = PassiveCollector.<Integer>builder().withMaxItems(1)
                .withMutableResults(false)
                .withListener(items -> {
                    executed.incrementAndGet();
                    Assertions.assertThrows(Exception.class, () -> {
                        items.add(1);
                    });
                })
                .build();

        collector.add(1);
        collector.flush();
        Assertions.assertEquals(1, executed.get());
    }

    @Test
    public void shoulNotBuiltWithoutSetup() throws Throwable {
        Assertions.assertThrows(Exception.class, () -> {
            PassiveCollector.<Integer>builder().build();
        });
    }
}