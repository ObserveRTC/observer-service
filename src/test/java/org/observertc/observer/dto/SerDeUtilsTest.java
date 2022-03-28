package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SerDeUtilsTest {

    @Test
    void checkEscapedStringEquality() {
        Assertions.assertNotEquals(SerDeUtils.NULL_STRING, SerDeUtils.ESCAPED_NULL_STRING);
    }
}