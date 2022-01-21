package org.observertc.webrtc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

@MicronautTest
class IndexedMapTest {

    @Test
    public void putAndRemoveEntriesWithoutIndex() {
        var one = "1";
        var map = new IndexedMap<Integer, String>();

        map.put(1, one);

        Assertions.assertFalse(map.isEmpty());
        Assertions.assertEquals(one, map.get(1));

        var removedOne = map.remove(1);
        Assertions.assertEquals(removedOne, one);
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void putAndRemoveEntriesWithIndex() {
        var indexKey = "myIndex";
        var one = "1";
        var map = new IndexedMap<Integer, String>().addIndex()
                .withMapper(str -> Integer.parseInt(str))
                .withName(indexKey)
                .add();

        map.put(1, one);

        Assertions.assertFalse(map.isEmpty());
        Assertions.assertEquals(one, map.get(1));

        var removedOne = map.remove(1);
        Assertions.assertEquals(removedOne, one);
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void getValuesWithoutIndex() {
        var one = "1";
        var map = new IndexedMap<Integer, String>();

        map.put(1, one);

        var values = map.values().stream().collect(Collectors.toList());

        Assertions.assertEquals(one, values.get(0));
    }

    @Test
    public void getValuesWithIndex() {
        var indexKey = "myIndex";
        var one = "1";
        var two = "1";
        var map = new IndexedMap<Integer, String>().addIndex()
                .withMapper(str -> Integer.parseInt(str))
                .withName(indexKey)
                .add();

        map.put(1, one);
        map.put(2, two);

        var values = map.values().stream().collect(Collectors.toList());

        Assertions.assertEquals(2, values.size());
    }

    @Test
    public void getValuesAfterRemoveWithoutIndex() {
        var indexKey = "myIndex";
        var one = "1";
        var two = "2";
        var map = new IndexedMap<Integer, String>();

        map.put(1, one);
        map.put(2, two);
        map.remove(2);

        var values = map.values().stream().collect(Collectors.toList());

        Assertions.assertEquals(one, values.get(0));
    }

    @Test
    public void getValuesAfterRemoveWithIndex() {
        var indexKey = "myIndex";
        var one = "1";
        var two = "2";
        var map = new IndexedMap<Integer, String>().addIndex()
                .withMapper(str -> Integer.parseInt(str))
                .withName(indexKey)
                .add();

        map.put(1, one);
        map.put(2, two);
        map.remove(2);

        var values = map.values(indexKey, 1).stream().collect(Collectors.toList());

        Assertions.assertEquals(one, values.get(0));
        Assertions.assertEquals(1, values.size());
    }

    @Test
    public void getKeysWithoutIndex() {
        var one = "1";
        var map = new IndexedMap<Integer, String>();

        map.put(1, one);

        var keys = map.keySet().stream().collect(Collectors.toList());

        Assertions.assertTrue(keys.contains(1));
        Assertions.assertEquals(1, keys.size());
    }

    @Test
    public void getKeysWithIndex() {
        var indexKey = "myIndex";
        var one = "1";
        var two = "1";
        var map = new IndexedMap<Integer, String>().addIndex()
                .withMapper(str -> Integer.parseInt(str))
                .withName(indexKey)
                .add();

        map.put(1, one);
        map.put(2, two);

        var keys = map.keySet(indexKey, 1).stream().collect(Collectors.toList());

        Assertions.assertTrue(keys.contains(1));
        Assertions.assertTrue(keys.contains(2));
        Assertions.assertEquals(2, keys.size());
    }
}