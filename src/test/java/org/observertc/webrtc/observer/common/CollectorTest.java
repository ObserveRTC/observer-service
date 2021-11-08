package org.observertc.webrtc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class CollectorTest {

    @Test
    public void shouldCollect_1() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxItems(2).build();

        collector.observableItems().subscribe(items -> {
            Assertions.assertEquals(1, items.get(0));
            Assertions.assertEquals(2, items.get(1));
            Assertions.assertEquals(2, items.size());
        });

        collector.addAll(1, 2);
    }

    @Test
    public void shouldCollect_2() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxTime(200).build();
        AtomicBoolean done = new AtomicBoolean(false);
        collector.observableItems().subscribe(items -> {
            Assertions.assertEquals(2, items.size());
            done.set(true);
        });

        collector.addAll(1, 2);
        new Sleeper(() -> 500).run();
        Assertions.assertTrue(done.get());
    }

    @Test
    public void shouldCollectAndRestart_1() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxItems(1).build();
        AtomicInteger executed = new AtomicInteger(0);
        collector.observableItems().subscribe(items -> {
            executed.incrementAndGet();
            Assertions.assertEquals(1, items.size());
            Assertions.assertEquals(executed.get(), items.get(0));
        });

        collector.addAll(1, 2, 3, 4, 5);
        Assertions.assertEquals(5, executed.get());
    }

    @Test
    public void shouldCollectAndRestart_2() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxTime(200).build();
        AtomicInteger executed = new AtomicInteger();
        collector.observableItems().subscribe(items -> {
            Assertions.assertEquals(2, items.size());
            executed.incrementAndGet();
        });

        collector.addAll(1, 2);
        new Sleeper(() -> 500).run();
        collector.addAll(3, 4);
        new Sleeper(() -> 500).run();
        Assertions.assertEquals(2, executed.get());
    }

    @Test
    public void shouldCollectAndRestart_3() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxTime(200).withMaxItems(2).build();
        AtomicInteger executed = new AtomicInteger();
        collector.observableItems().subscribe(items -> {
            executed.incrementAndGet();
        });

        collector.addAll(1, 2, 3, 4);
        new Sleeper(() -> 500).run();
        collector.addAll(5);
        new Sleeper(() -> 500).run();
        Assertions.assertEquals(3, executed.get());
    }

    @Test
    public void shouldEmitOnClose_1() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxItems(2).build();
        AtomicInteger executed = new AtomicInteger();
        collector.observableItems().subscribe(items -> {
            executed.incrementAndGet();
        });

        collector.addAll(1);
        collector.close();
        Assertions.assertEquals(1, executed.get());
    }

    @Test
    public void valhalla_1() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxItems(100).build();
        AtomicInteger executed = new AtomicInteger(0);
        collector.observableItems().subscribe(items -> {
            executed.incrementAndGet();
        });
        AtomicBoolean run = new AtomicBoolean(true);
        for(int i=0; i<5; i++){
            var thread = new Thread("" + i){
                public void run(){
                    while (run.get()) {
                        int value = getRandom(1, 10000);
                        try {
                            collector.add(value);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }
                }
            };
            thread.start();
        }
        new Sleeper(() -> 2000).run();
        Assertions.assertTrue(0 < executed.get());
        System.out.println("Executed:" + executed.get());
        run.set(false);
    }

    @Test
    public void valhalla_2() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxTime(100).build();
        AtomicInteger executed = new AtomicInteger(0);
        collector.observableItems().subscribe(items -> {
            executed.incrementAndGet();
        });
        AtomicBoolean run = new AtomicBoolean(true);
        for(int i=0; i<5; i++){
            var thread = new Thread("" + i){
                public void run(){
                    while (run.get()) {
                        int value = getRandom(1, 10);
                        try {
                            collector.add(value);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }
                }
            };
            thread.start();
        }
        new Sleeper(() -> 2000).run();
        run.set(false);
        Assertions.assertTrue(0 < executed.get());
    }

    @Test
    public void valhalla_3() throws Throwable {
        var collector = Collector.<Integer>builder().withMaxTime(100).withMaxItems(100).build();
        AtomicInteger executed = new AtomicInteger(0);
        collector.observableItems().subscribe(items -> {
            executed.incrementAndGet();
        });
        AtomicBoolean run = new AtomicBoolean(true);
        for(int i=0; i<5; i++){
            var thread = new Thread("" + i){
                public void run(){
                    while (run.get()) {
                        int value = getRandom(1, 10);
                        try {
                            collector.add(value);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }
                }
            };
            thread.start();
        }
        new Sleeper(() -> 2000).run();
        run.set(false);
        Assertions.assertTrue(0 < executed.get());
    }

    private static int getRandom(int min, int max) {
        int range = max - min + 1;
        return (int)(Math.random() * range) + min;
    }
}