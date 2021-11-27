package org.observertc.webrtc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

@MicronautTest
class TopologicalTaskTest {

    @Test
    void shouldExecuteInOrder() {
        AtomicInteger lastExecutedId = new AtomicInteger(0);
        var task = new TopologicalTask();
        var one = this.makeTask(() -> {
            Assertions.assertEquals(1, lastExecutedId.incrementAndGet());
        });
        var two = this.makeTask(() -> {
            Assertions.assertEquals(2, lastExecutedId.incrementAndGet());
        });
        var three = this.makeTask(() -> {
            Assertions.assertEquals(3, lastExecutedId.incrementAndGet());
        });

        task.withTask(two, one);
        task.withTask(three, two);
        task.execute();

        Assertions.assertEquals(3, lastExecutedId.get());
    }

    @Test
    void shouldThrowExceptionIfCircularDependencyFound() {
        var task = new TopologicalTask();
        var one = this.makeTask(() -> {});
        var two = this.makeTask(() -> {});

        task.withTask(two, one);
        task.withTask(one, two);

        Assertions.assertThrows(Exception.class, () -> {
            task.execute();
        });
    }

    private Task makeTask(Runnable onExecuted) {
        var result = new TaskAbstract<Void>() {
            @Override
            protected Void perform() {
                onExecuted.run();
                return null;
            }
        };
        return result;
    };

}