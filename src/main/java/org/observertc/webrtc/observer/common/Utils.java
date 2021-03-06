package org.observertc.webrtc.observer.common;

import java.util.Arrays;
import java.util.Objects;

public class Utils {

    public static<T> T ifExpectedThenAlternative(T subject, T expected, T alternative) {
        return !Objects.equals(subject, expected) ? expected : alternative;
    }

    public static boolean anyNull(Object... objects) {
        Objects.requireNonNull(objects, "To determine if any object is null, we need objects");
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }
}
