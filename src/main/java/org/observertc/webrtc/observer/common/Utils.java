package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.functions.Function;

import java.util.*;
import java.util.function.Consumer;

public class Utils {
//    public static final String UNKOWN_TAG_VALUE;

    public static<T> T ifExpectedThenAlternative(T subject, T expected, T alternative) {
        return !Objects.equals(subject, expected) ? expected : alternative;
    }

    public static<T> Collection<T> coalesceCollection(Collection<T> actualValue) {
        return Objects.nonNull(actualValue) ? actualValue : Collections.EMPTY_LIST;
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

    public static boolean allNull(Object... objects) {
        Objects.requireNonNull(objects, "To determine if all object is null, we need objects");
        return Arrays.stream(objects).allMatch(Objects::isNull);
    }

    public static<T> boolean isListNotEmpty(List<T> list) {
        return Objects.nonNull(list) && 0 < list.size();
    }

    public static<T> boolean isListEmptyOrNull(List<T> list) {
        return Objects.isNull(list) || list.size() < 1;
    }

    public static boolean nullOrFalse(Boolean value) {
        return Objects.isNull(value) || value == false;
    }

    public static<T> Function<T, T> createPrintingMapper(String context) {
        return input -> {
            String message = String.format("[%s]: %s", context, input.toString());
            System.out.println(message);
            return input;
        };
    }
}
