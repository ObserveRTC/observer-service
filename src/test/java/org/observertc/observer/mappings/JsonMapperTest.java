package org.observertc.observer.mappings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonMapperTest {

    @Test
    void shouldMapObjToStringAndBack() {
        var toString = JsonMapper.createObjectToStringMapper();
        var fromString = JsonMapper.createStringToObjectMapper(Foo.class);
        var expected = new Foo(); expected.bar = 2;
        var actual = fromString.map(toString.map(expected));

        Assertions.assertEquals(2, actual.bar);

    }

    @Test
    void shouldMapObjToBytesAndBack() {
        var toBytes = JsonMapper.createObjectToBytesMapper();
        var fromBytes = JsonMapper.createBytesToObjectMapper(Foo.class);
        var expected = new Foo(); expected.bar = 2;
        var actual = fromBytes.map(toBytes.map(expected));

        Assertions.assertEquals(2, actual.bar);

    }

    @Test
    void bytesToObjectCodecShouldWork() {
        var codec= JsonMapper.createBytesToObjectCodec(Foo.class);
        var expected = new Foo(); expected.bar = 2;
        var actual = codec.decode(codec.encode(expected));

        Assertions.assertEquals(2, actual.bar);

    }

    @Test
    void stringToObjectCodecShouldWork() {
        var codec= JsonMapper.createStringToObjectCodec(Foo.class);
        var expected = new Foo(); expected.bar = 2;
        var actual = codec.decode(codec.encode(expected));

        Assertions.assertEquals(2, actual.bar);

    }

    public static class Foo {
        public int bar;
    }
}