package org.observertc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@MicronautTest
class WeakLockDTOTest {

    @Test
    void shouldBuild_1() throws InterruptedException {
        Instant before = Instant.now();
        Thread.sleep(10);
        WeakLockDTO weakLockDTO = WeakLockDTO.of("myLock", "myInstance");
        Thread.sleep(10);
        Instant after = Instant.now();

        Assertions.assertEquals("myLock", weakLockDTO.name);
        Assertions.assertEquals("myInstance", weakLockDTO.instance);
        Assertions.assertTrue(before.compareTo(weakLockDTO.created) < 0);
        Assertions.assertTrue(0 < after.compareTo(weakLockDTO.created));
    }

    @Test
    void shouldBeNotEqual_1() throws InterruptedException {
        WeakLockDTO weakLockDTO_1 = WeakLockDTO.of("myLock_1", "myInstance_1");
        Thread.sleep(1000);
        WeakLockDTO weakLockDTO_2 = WeakLockDTO.of("myLock_1", "myInstance_1");

        boolean assertion_1 = weakLockDTO_1.equals(weakLockDTO_2);
        Assertions.assertFalse(assertion_1);
    }

    @Test
    void shouldBeNotEqual_2() {
        WeakLockDTO weakLockDTO_1 = WeakLockDTO.of("myLock_1", "myInstance_1");
        WeakLockDTO weakLockDTO_2 = WeakLockDTO.of("myLock_1", "myInstance_2");
        WeakLockDTO weakLockDTO_3 = WeakLockDTO.of("myLock_2", "myInstance_1");

        boolean assertion_1 = weakLockDTO_1.equals(weakLockDTO_2);
        Assertions.assertFalse(assertion_1);
        boolean assertion_2 = weakLockDTO_1.equals(weakLockDTO_3);
        Assertions.assertFalse(assertion_2);
    }

}