package org.observertc.webrtc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

class TaskStageTest {

    @Test
    void shouldRunAction() throws Throwable {
        AtomicBoolean result = new AtomicBoolean(false);
        TaskStage stage = TaskStage
                .builder("test")
                .withAction(() -> result.set(true))
                .build();

        stage.execute(null);

        Assertions.assertTrue(result.get());
    }

//    @Test
//    void shouldRunConsumer() throws Throwable {
//        Boolean result = false;
//        TaskStage stage = TaskStage
//                .builder("test")
//                .withConsumer(ref -> ref = true)
//                .build();
//
//        stage.execute(new AtomicReference(result));
//
//        Assertions.assertTrue(result);
//    }
}