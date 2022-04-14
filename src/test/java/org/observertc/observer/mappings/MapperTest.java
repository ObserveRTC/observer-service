package org.observertc.observer.mappings;

import com.google.protobuf.InvalidProtocolBufferException;
import io.reactivex.rxjava3.functions.Function;
import org.bson.internal.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.schemas.protobuf.ProtobufSamples;

class MapperTest {

    @Test
    void shouldCreate() {
        var mapper = Mapper.<Integer, String>create(Integer::toUnsignedString);

        Assertions.assertEquals("1", mapper.map(1));
    }

    @Test
    void shouldCreateFromRxFunc() {
        Function<Integer, String> func = num -> num.toString();
        var mapper = Mapper.createFromRxFunc(func);

        Assertions.assertEquals("1", mapper.map(1));
    }

    @Test
    void shouldMapBase64AndBack() {
        var fromBase64 = Mapper.createBytesToBase64Mapper();
        var toBase64 = Mapper.createBase64Mapper();

        byte[] expected = { 1 };
        var actual = fromBase64.map(toBase64.map(expected));
        Assertions.assertArrayEquals(expected,actual);
    }

    @Test
    void shouldMapStringToBytes() {
        var fromString = Mapper.createStringToBytesMapper();
        var toString = Mapper.createBytesToStringMapper();

        byte[] expected = { 1 };
        var actual = fromString.map(toString.map(expected));
        Assertions.assertArrayEquals(expected,actual);
    }

    @Test
    void shouldLink() {
        var strToInt = Mapper.<String, Integer>create(Integer::parseInt);
        var intToBool = Mapper.<Integer, Boolean>create(num -> num % 2 == 0);
        var mapper = Mapper.<String, Integer, Boolean>link(strToInt, intToBool);

        Assertions.assertEquals(true, mapper.map("6"));
        Assertions.assertEquals(false, mapper.map("5"));
    }


}