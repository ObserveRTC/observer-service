package org.observertc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class TryTest {

    @Test
    void tryRunThreeTimes() {
        var invoked = new AtomicInteger(0);
        Try.wrap(() -> {
            invoked.incrementAndGet();
            throw new RuntimeException();
        });
        Assertions.assertEquals(3, invoked.get());
    }

    @Test
    void callOnExceptionListener() {
        var invoked = new AtomicBoolean(false);
        Try.create(() -> {
            throw new RuntimeException();
        }).onException(err -> invoked.set(true)).run();
        Assertions.assertTrue(invoked.get());
    }

    @Test
    void supplyInCaseOfNormalExecution() {
        var result = Try.wrap(() -> 1, 0);
        Assertions.assertEquals(1, result);
    }

    @Test
    void supplyInCaseOfTwoTimesExecution() {
        var invoked = new AtomicInteger(0);
        var result = Try.wrap(() -> {
            if (invoked.incrementAndGet() < 2) {
                throw new RuntimeException();
            }
            return 1;
        }, 0);
        Assertions.assertEquals(1, result);
    }

    @Test
    void supplyInCaseOfExecution() {
        var result = Try.wrap(() -> {
            throw new RuntimeException();
        }, 0);
        Assertions.assertEquals(0, result);
    }
}