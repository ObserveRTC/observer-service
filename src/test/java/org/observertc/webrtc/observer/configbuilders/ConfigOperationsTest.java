package org.observertc.webrtc.observer.configbuilders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigOperationsTest {

    @Test
    public void shouldAssign_1() throws IOException {
        Map values = Map.of("a", 2);
        ConfigOperations configOps = new ConfigOperations(Map.of("a", 1));

        configOps.add(values);

        Assertions.assertTrue(assertMaps(configOps.makeConfig(), Map.of("a", 2)));
    }

    @Test
    public void shouldAssign_2() throws IOException {
        Map values = Map.of("a", Map.of("b", 2));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", Map.of("b", 1)));

        configOps.add(values);

        Assertions.assertTrue(assertMaps(configOps.makeConfig(), Map.of("a", Map.of("b", 2))));
    }

    @Test
    public void shouldAssign_3() throws IOException {
        Map values = Map.of("a", List.of(1, 2));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", List.of(1, 3)));

        configOps.add(values);

        Assertions.assertTrue(assertMaps(configOps.makeConfig(), Map.of("a", List.of(1, 2, 3))));
    }

    @Test
    public void shouldNotAssign_withoutPredicate() throws IOException {
        Map values = Map.of("a", List.of(Map.of("b", 2), 2));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", List.of(Map.of("b", 1), 3)));

        configOps.add(values);

        Assertions.assertFalse(assertMaps(configOps.makeConfig(), Map.of("a", List.of(Map.of("b", 2), 2, 3))));
    }

    @Test
    public void shouldAssign_withPredicate() throws IOException {
        Map values = Map.of("a", List.of(Map.of("b", 2), 2));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", List.of(Map.of("b", 1), 3)))
                .withIndexOfPredicate(List.of("a"), (list, o) -> {
                    if (o instanceof Map) {
                        for (int i = 0; i < list.size(); ++i) {
                            Object p = list.get(i);
                            if (p instanceof Map) {
                                if ( ((Map<?, ?>) p).get("b") != null) return i;
                            }
                        }
                        return -1;
                    }
                    return list.indexOf(o);
                });

        configOps.add(values);

        Assertions.assertTrue(assertMaps(configOps.makeConfig(), Map.of("a", List.of(Map.of("b", 2), 2, 3))));
    }

    @Test
    public void shouldAssign_withPredicate_2() throws IOException {
        Map values = Map.of("a", List.of(Map.of("b", Map.of("c", 2)), 2));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", List.of(Map.of("b", Map.of("d", 3)), 3)))
                .withIndexOfPredicate(List.of("a"), (list, o) -> {
                    if (o instanceof Map) {
                        for (int i = 0; i < list.size(); ++i) {
                            Object p = list.get(i);
                            if (p instanceof Map) {
                                if ( ((Map<?, ?>) p).get("b") != null) return i;
                            }
                        }
                        return -1;
                    }
                    return list.indexOf(o);
                });

        configOps.add(values);

        Assertions.assertTrue(assertMaps(configOps.makeConfig(), Map.of("a", List.of(Map.of("b", Map.of("c", 2, "d", 3)), 2, 3))));
    }

    private boolean assertMaps(Map a, Map b) {
        Iterator<Map.Entry> it = a.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object peer = b.get(key);
            if (!assertValues(value, peer)) return false;
        }

        it = b.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object peer = a.get(key);
            if (!assertValues(value, peer)) return false;
        }
        return true;
    }

    private boolean assertLists(List a, List b) {
        Iterator it = a.iterator();
        while (it.hasNext()) {
            Object valueA = it.next();
            int index = b.indexOf(valueA);
            if (index < 0) return false;
            Object valueB = b.get(index);
            if (!assertValues(valueA, valueB)) return false;
        }

        it = b.iterator();
        while (it.hasNext()) {
            Object valueB = it.next();
            int index = a.indexOf(valueB);
            if (index < 0) return false;
            Object valueA = a.get(index);
            if (!assertValues(valueA, valueB)) return false;
        }
        return true;
    }

    private boolean assertValues(Object a, Object b) {
        if (a == null && b != null) return false;
        if (b == null && a != null) return false;
        if (a instanceof Map && b instanceof Map) return assertMaps((Map) a, (Map) b);
        if (a instanceof List && b instanceof List) return assertLists((List) a, (List) b);
        return a.equals(b);
    }
}