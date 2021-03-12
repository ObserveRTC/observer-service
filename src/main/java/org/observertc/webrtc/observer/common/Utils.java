package org.observertc.webrtc.observer.common;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class Utils {

    public static<T> T ifExpectedThenAlternative(T subject, T expected, T alternative) {
        return !Objects.equals(subject, expected) ? expected : alternative;
    }

    public static<T> void runIfNonNull(T value, Runnable action) {
        if (Objects.nonNull(value)) {
            action.run();
        }
    }

    public static<T> void execIfValueNonNull(T value, Consumer<T> action) {
        if (Objects.nonNull(value)) {
            action.accept(value);
        }
    }

    public static boolean anyNull(Object... objects) {
        Objects.requireNonNull(objects, "To determine if any object is null, we need objects");
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }
}
