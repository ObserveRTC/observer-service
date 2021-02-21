package org.observertc.webrtc.observer.configbuilders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

class ConfigConverterTest {

    @Test
    void shouldConvertToExample_1() {
        Map<String, Object> values = Map.of("key", 1);

        Example example = ConfigConverter.convert(Example.class, values);

        Assertions.assertEquals(1, example.key);
    }

    @Test
    void shouldFailBecauseOfViolation_1() {
        Map<String, Object> values = Map.of("key", -1);

        Assertions.assertThrows(Exception.class, () -> ConfigConverter.convert(Example.class, values));
    }

    @Test
    void shouldConvertExample_2() {
        Map<String, Object> values = new HashMap<>(Map.of("something-not-camel-cased", 1));

        ConfigConverter.forceKeysToBeCamelCase(values);

        Assertions.assertEquals(1, values.get("somethingNotCamelCased"));
    }

    @Test
    void shouldConvertExample_3() {
        Map<String, Object> values = new HashMap<>(Map.of("something-not-camel-cased-but-ends-with-uuid", 1));

        ConfigConverter.forceKeysToBeCamelCase(values);

        Assertions.assertEquals(1, values.get("somethingNotCamelCasedButEndsWithUUID"));
    }

    @Test
    void shouldConvertExample_4() {
        Map<String, Object> values = new HashMap<>(Map.of("something-not-camel-cased-and-ends-with-id", 1));

        ConfigConverter.forceKeysToBeCamelCase(values);

        Assertions.assertEquals(1, values.get("somethingNotCamelCasedAndEndsWithId"));
    }

    public static class Example {

        @Min(0)
        public int key = 0;
    }

}