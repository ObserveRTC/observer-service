package org.observertc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


class UtilsTest {

    @Test
    public void ifSubjectIsExpectedOrAlternativeTest_1() {
        var actual = Utils.ifSubjectIsExpectedOrAlternative(1, 1, 2);
        Assertions.assertEquals(1, actual);
    }

    @Test
    public void ifSubjectIsExpectedOrAlternativeTest_2() {
        var actual = Utils.ifSubjectIsExpectedOrAlternative(2, 1, 3);
        Assertions.assertEquals(3, actual);
    }


    @Test
    public void supplyFirstNotNullTest_1() {
        var actual = Utils.supplyFirstNotNull(() -> 1);
        Assertions.assertEquals(1, actual);
    }

    @Test
    public void supplyFirstNotNullTest_2() {
        var actual = Utils.supplyFirstNotNull(null, () -> 2);
        Assertions.assertEquals(2, actual);
    }

    @Test
    public void supplyFirstNotNullTest_3() {
        var actual = Utils.supplyFirstNotNull(null, () -> null, () -> 3);
        Assertions.assertEquals(3, actual);
    }


    @Test
    public void runIfValueNonNullTest_1() {
        var actual = new AtomicBoolean(false);
        Utils.runIfValueNonNull(1, () -> actual.set(true));
        Assertions.assertEquals(true, actual.get());
    }

    @Test
    public void runIfValueNonNullTest_2() {
        var actual = new AtomicBoolean(false);
        Utils.runIfValueNonNull(null, () -> actual.set(true));
        Assertions.assertEquals(false, actual.get());
    }

    @Test
    public void acceptIfValueNonNullTest_1() {
        var actual = new AtomicBoolean(false);
        Utils.acceptIfValueNonNull(true,actual::set);
        Assertions.assertEquals(true, actual.get());
    }

    @Test
    public void acceptIfValueNonNullTest_2() {
        var actual = new AtomicBoolean(false);
        Utils.acceptIfValueNonNull(null, actual::set);
        Assertions.assertEquals(false, actual.get());
    }

    @Test
    public void isCollectionNotEmptyTest_1() {
        var actual = Utils.isCollectionNotEmpty(null);
        Assertions.assertEquals(false, actual);
    }

    @Test
    public void isCollectionNotEmptyTest_2() {
        var actual = Utils.isCollectionNotEmpty(Collections.EMPTY_LIST);
        Assertions.assertEquals(false, actual);
    }

    @Test
    public void isCollectionNotEmptyTest_3() {
        var actual = Utils.isCollectionNotEmpty(List.of(1));
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void isCollectionEmptyOrNullTest_1() {
        var actual = Utils.isCollectionEmptyOrNull(null);
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void isCollectionEmptyOrNullTest_2() {
        var actual = Utils.isCollectionEmptyOrNull(Collections.EMPTY_LIST);
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void isCollectionEmptyOrNullTest_3() {
        var actual = Utils.isCollectionEmptyOrNull(List.of(1));
        Assertions.assertEquals(false, actual);
    }

    @Test
    public void nullOrFalseTest_1() {
        var actual = Utils.nullOrFalse(null);
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void nullOrFalseTest_2() {
        var actual = Utils.nullOrFalse(false);
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void nullOrFalseTest_3() {
        var actual = Utils.nullOrFalse(true);
        Assertions.assertEquals(false, actual);
    }
}