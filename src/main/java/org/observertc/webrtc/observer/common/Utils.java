package org.observertc.webrtc.observer.common;

import java.util.Objects;

public class Utils {

    public static<T> T ifExpectedThenAlternative(T subject, T expected, T alternative) {
        return !Objects.equals(subject, expected) ? expected : alternative;
    }
}
