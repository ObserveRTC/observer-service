package org.observertc.observer.mappings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CodecTest {

    @Test
    void shouldCreateACodec() {
        var codec = Codec.<Integer, String>create(
                Integer::toUnsignedString,
                Integer::parseInt
        );

        var expected = 5;
        var actual = codec.decode(codec.encode(expected));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldCreateACodecFromMapper() {
        var intToStr = Mapper.<Integer, String>create(Integer::toUnsignedString);
        var strToInt = Mapper.<String, Integer>create(Integer::parseInt);
        var codec = Codec.create(intToStr, strToInt);

        var expected = 5;
        var actual = codec.decode(codec.encode(expected));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldLinkCodecs() {
        var strToInt = Codec.<String, Integer>create(
                Integer::parseInt,
                Integer::toUnsignedString
        );
        var intToBoolean = Codec.<Integer, Boolean>create(
                num -> num % 2 == 0,
                bool -> bool ? 1 : 0
        );

        var codec = Codec.link(strToInt, intToBoolean);

        Assertions.assertEquals(false, codec.encode("1"));
        Assertions.assertEquals(true, codec.encode("2"));
        Assertions.assertEquals("0", codec.decode(false));
        Assertions.assertEquals("1", codec.decode(true));

    }
}