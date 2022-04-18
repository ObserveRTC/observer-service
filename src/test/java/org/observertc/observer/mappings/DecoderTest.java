package org.observertc.observer.mappings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DecoderTest {

    @Test
    void shouldCreate() throws Throwable {
        var mapper = Mapper.<Integer, String>create(Integer::toUnsignedString);
        var decoder = Decoder.from(mapper);

        Assertions.assertEquals("1", decoder.decode(1));
    }
}