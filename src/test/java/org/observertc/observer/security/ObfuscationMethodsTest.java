package org.observertc.observer.security;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.configs.ObfuscationType;

@MicronautTest
class ObfuscationMethodsTest {

    @Inject
    ObfuscationMethods obfuscationMethods;

    @Test
    void shouldAnonimize() {
        var notExpected = "source";
        var obfuscator = obfuscationMethods
                .builder(ObfuscationType.ANONYMIZATION)
                .buildForString();
        var actual = obfuscator.apply(notExpected);
        Assertions.assertFalse(notExpected.equals(actual));
    }

    @Test
    void shouldBeSame() {
        var expected = "source";
        var obfuscator = obfuscationMethods
                .builder(ObfuscationType.NONE)
                .buildForString();
        var actual = obfuscator.apply(expected);
        Assertions.assertTrue(expected.equals(actual));
    }
}