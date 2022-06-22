package org.observertc.observer.common;

import io.reactivex.rxjava3.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Utils {
    private Utils() {

    }

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
//    public static final String UNKOWN_TAG_VALUE;

    public static<T> T ifSubjectIsExpectedOrAlternative(T subject, T expected, T alternative) {
        return Objects.equals(subject, expected) ? expected : alternative;
    }

    public static<T> T supplyFirstNotNull(Supplier<T>... suppliers) {
        if (Objects.isNull(suppliers)) return null;
        for (var supplier : suppliers) {
            if (Objects.isNull(supplier)) continue;
            var result = supplier.get();
            if (Objects.nonNull(result)) return result;
        }
        return null;
    }


//    public static<T> Collection<T> coalesceCollection(Collection<T> actualValue) {
//        return Objects.nonNull(actualValue) ? actualValue : Collections.EMPTY_LIST;
//    }

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

    public static<T> T firstNotNull(T... objects) {
        if (objects == null) return null;
        for (var item : objects) {
            if (item == null) continue;
            return item;
        }
        return null;
    }

    public static boolean allNull(Object... objects) {
        Objects.requireNonNull(objects, "To determine if all object is null, we need objects");
        return Arrays.stream(objects).allMatch(Objects::isNull);
    }

    public static<T> boolean isCollectionNotEmpty(Collection<T> collection) {
        return Objects.nonNull(collection) && 0 < collection.size();
    }

    public static<T> boolean isCollectionEmptyOrNull(Collection<T> collection) {
        return Objects.isNull(collection) || collection.size() < 1;
    }

    public static<T> Stream<T> trash(Stream<T> origin, Predicate<T> predicate, Collection<T> trashed) {
        var trash = makeTrash(predicate, trashed);
        return origin.filter(trash);
    }

    public static<T> Predicate<T> makeTrash(Predicate<T> predicate, Collection<T> trashed) {
        return new Predicate<T>() {
            @Override
            public boolean test(T t) {
                var result = predicate.test(t);
                if (!result) {
                    trashed.add(t);
                }
                return result;
            }
        };
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

    /**
     * Check if a a value is non null. Returns true if it is NOT null.
     * if it is null it gets the stacktrace and log as warning any related code line.
     * @param subject
     * @return true if the objects is not null, false otherwise
     */
    public static boolean expensiveNonNullCheck(Object subject) {
        if (Objects.nonNull(subject)) {
            return true;
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace == null) {
            logger.warn("Null value is detected where it does not supposed to be.");
            return false;
        }
        var stackTrackStr = Arrays.asList(Thread.currentThread().getStackTrace()).stream()
                .map(StackTraceElement::toString)
                .filter(str -> str.contains("observertc"))
                .collect(Collectors.joining("\n"));
        logger.warn("Null value is detected where it does not supposed to be. {}", stackTrackStr);
        return false;
    }

    public static String camelCaseToSnakeCase(String input) {
        var buffer = new StringBuffer();
        for (int i = 0; i < input.length(); ++i) {
            var letter = input.charAt(i);
            if (Character.isUpperCase(letter)) {
                buffer.append("_");
                buffer.append(Character.toLowerCase(letter));
            } else {
                buffer.append(letter);
            }
        }
        return buffer.toString();
    }
}
