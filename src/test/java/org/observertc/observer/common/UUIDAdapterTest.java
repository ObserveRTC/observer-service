package org.observertc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@MicronautTest
class UUIDAdapterTest {

    @Test
    void shouldEncodeDecodeEquals_1() {
        var source = UUID.randomUUID();
        var encoded = UUIDAdapter.toBytes(source);
        var decoded = UUIDAdapter.toUUID(encoded);

        Assertions.assertEquals(source, decoded);
    }

    @Test
    void shouldNotEncodeNull() {
        Assertions.assertThrows(Exception.class, () -> {
            var encoded = UUIDAdapter.toBytes(null);
        });
    }

    @Test
    void shouldNotDecodeNull() {
        Assertions.assertThrows(Exception.class, () -> {
            var encoded = UUIDAdapter.toUUID(null);
        });
    }

    @Test
    void shouldEncodeDecodeEquals_2() {
        var source = UUID.randomUUID();
        var encoded = UUIDAdapter.toBytesOrDefault(source, null);
        var decoded = UUIDAdapter.toUUIDOrDefault(encoded, null);

        Assertions.assertEquals(source, decoded);
    }

    @Test
    void shouldNotThrowExceptionIfInputIsNull_1() {
        var encoded = UUIDAdapter.toBytesOrDefault(null, null);

        Assertions.assertNull(encoded);
    }

    @Test
    void shouldNotThrowExceptionIfInputIsNull_2() {
        var encoded = UUIDAdapter.toUUIDOrDefault(null, null);

        Assertions.assertNull(encoded);
    }

    @Test
    void shouldNotThrowExceptionIfInputIsNull_3() {
        var encoded = UUIDAdapter.toStringOrDefault(null, null);

        Assertions.assertNull(encoded);
    }

    @Test
    void shouldNotThrowExceptionIfInputIsNull_4() {
        var encoded = UUIDAdapter.toStringOrNull(null);

        Assertions.assertNull(encoded);
    }

    @Test
    void shouldConvertToString_1() {
        var source = UUID.randomUUID();
        var encoded = UUIDAdapter.toStringOrDefault(source, null);

        Assertions.assertEquals(source.toString(), encoded);
    }

    @Test
    void shouldConvertToString_2() {
        var source = UUID.randomUUID();
        var encoded = UUIDAdapter.toStringOrNull(source);

        Assertions.assertEquals(source.toString(), encoded);
    }

    @Test
    void shouldConvertToOptionalUUID_1() {
        var source = UUID.randomUUID();
        var encoded = UUIDAdapter.tryParse(source.toString());

        Assertions.assertTrue(encoded.isPresent());
        Assertions.assertEquals(source, encoded.get());
    }

    @Test
    void shouldConvertToOptionalUUID_2() {
        var encoded = UUIDAdapter.tryParse(null);

        Assertions.assertTrue(encoded.isEmpty());
    }
}