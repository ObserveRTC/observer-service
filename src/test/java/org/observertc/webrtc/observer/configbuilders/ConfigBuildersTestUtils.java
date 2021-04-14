package org.observertc.webrtc.observer.configbuilders;

import org.junit.jupiter.api.Assertions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigBuildersTestUtils {

    public static void assertMapsEqual(Map a, Map b) {
        Assertions.assertTrue(isMapsEquals(a, b));
    }

    public static boolean isMapsEquals(Map a, Map b) {
        Iterator<Map.Entry> it = a.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object peer = b.get(key);
            if (!isValuesEquals(value, peer)) return false;
        }

        it = b.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object peer = a.get(key);
            if (!isValuesEquals(value, peer)) return false;
        }
        return true;
    }

    private static boolean isListsEquals(List a, List b) {
        Iterator it = a.iterator();
        while (it.hasNext()) {
            Object valueA = it.next();
            int index = b.indexOf(valueA);
            if (index < 0) return false;
            Object valueB = b.get(index);
            if (!isValuesEquals(valueA, valueB)) return false;
        }

        it = b.iterator();
        while (it.hasNext()) {
            Object valueB = it.next();
            int index = a.indexOf(valueB);
            if (index < 0) return false;
            Object valueA = a.get(index);
            if (!isValuesEquals(valueA, valueB)) return false;
        }
        return true;
    }

    private static boolean isValuesEquals(Object a, Object b) {
        if (a == null && b != null) return false;
        if (b == null && a != null) return false;
        if (a instanceof Map && b instanceof Map) return isMapsEquals((Map) a, (Map) b);
        if (a instanceof List && b instanceof List) return isListsEquals((List) a, (List) b);
        return a.equals(b);
    }
}
