package org.observertc.webrtc.observer.common;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class Utils {

//    public static final String UNKOWN_TAG_VALUE;

    public static<T> T ifExpectedThenAlternative(T subject, T expected, T alternative) {
        return !Objects.equals(subject, expected) ? expected : alternative;
    }

    public static<T> void runIfValueNonNull(T value, Runnable action) {
        if (Objects.nonNull(value)) {
            action.run();
        }
    }

    public static<T> void acceptIfValueNonNull(T value, Consumer<T> action) {
        if (Objects.nonNull(value)) {
            action.accept(value);
        }
    }

    public static boolean anyNull(Object... objects) {
        Objects.requireNonNull(objects, "To determine if any object is null, we need objects");
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }
}
