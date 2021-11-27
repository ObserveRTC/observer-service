package org.observertc.webrtc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

@MicronautTest
class BatchCollectorTest {


    @Test
    void shouldEmitAll_1() {
        AtomicInteger expected = new AtomicInteger(1);
        Stream.of(1,2,3,4,5,6).collect(BatchCollector.<Integer>builder()
                .withBatchSize(3)
                .withConsumer(new Consumer<List>() {
                    @Override
                    public void accept(List list) {
                        list.stream().forEach(actual -> {
                            Assertions.assertEquals(expected.getAndIncrement(), actual);
                        });
                    }
                })
                .build());

        Assertions.assertEquals(7, expected.get());
    }

    @Test
    void shouldEmitAll_2() {
        AtomicInteger expected = new AtomicInteger(1);
        Stream.of(1,2,3,4,5).collect(BatchCollector.<Integer>builder()
                .withBatchSize(3)
                .withConsumer(new Consumer<List>() {
                    @Override
                    public void accept(List list) {
                        list.stream().forEach(actual -> {
                            Assertions.assertEquals(expected.getAndIncrement(), actual);
                        });
                    }
                })
                .build());

        Assertions.assertEquals(6, expected.get());
    }

    @Test
    void shouldEmitBatches_1() {
        AtomicInteger invoked = new AtomicInteger(0);
        Stream.of(1,2,3,4,5,6).collect(BatchCollector.<Integer>builder()
                .withBatchSize(3)
                .withConsumer(batch -> {
                    invoked.incrementAndGet();
                })
                .build());

        Assertions.assertEquals(2, invoked.get());
    }

    @Test
    void shouldEmitBatches_2() {
        AtomicInteger invoked = new AtomicInteger(0);
        Stream.of(1,2,3,4,5,6,7).collect(BatchCollector.<Integer>builder()
                .withBatchSize(3)
                .withConsumer(batch -> {
                    invoked.incrementAndGet();
                })
                .build());

        Assertions.assertEquals(3, invoked.get());
    }

}