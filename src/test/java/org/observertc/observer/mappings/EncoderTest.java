package org.observertc.observer.mappings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EncoderTest {

    @Test
    void shouldCreate() throws Throwable {
        var mapper = Mapper.<Integer, String>create(Integer::toUnsignedString);
        var encoder = Encoder.from(mapper);

        Assertions.assertEquals("1", encoder.encode(1));
    }

}