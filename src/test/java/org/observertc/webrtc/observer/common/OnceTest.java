package org.observertc.webrtc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class OnceTest {

    @Test
    void shouldNotGetNotSetValue() {
        Once<Integer> once = new Once<>();

        Assertions.assertThrows(Exception.class, () -> {
            once.get();
        });
    }


    @Test
    void shouldHaveIndicator() {
        Once<Integer> once = new Once<>();

        Assertions.assertFalse(once.isSet());
    }


    @Test
    void shouldSetValue() {
        Once<Integer> once = new Once<>();

        once.set(1);

        Assertions.assertThrows(Exception.class, () -> {
            once.set(2);
        });
    }

    @Test
    void shouldSetValueAndHaveIndicator() {
        Once<Integer> once = new Once<>();

        once.set(1);

        Assertions.assertTrue(once.isSet());
    }

    @Test
    void shouldGetValue() {
        Once<Integer> once = new Once<>();

        once.set(1);

        Assertions.assertEquals(1, once.get());
    }

    @Test
    void shouldSetEvenNull() {
        Once<Integer> once = new Once<>();

        once.set(null);

        Assertions.assertThrows(Exception.class, () -> {
            once.set(null);
        });
    }

    @Test
    void shouldGetEvenNull() {
        Once<Integer> once = new Once<>();

        once.set(null);

        Assertions.assertNull(once.get());
    }
}