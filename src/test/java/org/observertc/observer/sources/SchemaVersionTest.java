package org.observertc.observer.sources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaVersionTest {

    @Test
    public void shouldParseCorrectly_1() {
        final SchemaVersion version = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(version.getConceptVersion(), 1);
        Assertions.assertEquals(version.getSamplesVersion(), 2);
        Assertions.assertEquals(version.getReportsVersion(), 3);
    }

    @Test
    public void shouldParseCorrectly_2() {
        final SchemaVersion version = SchemaVersion.parse("\t 1.2.3");
        Assertions.assertEquals(version.getConceptVersion(), 1);
        Assertions.assertEquals(version.getSamplesVersion(), 2);
        Assertions.assertEquals(version.getReportsVersion(), 3);
    }

    @Test
    public void shouldParseCorrectly_3() {
        final SchemaVersion version = SchemaVersion.parse("1.2.3\t\n");
        Assertions.assertEquals(version.getConceptVersion(), 1);
        Assertions.assertEquals(version.getSamplesVersion(), 2);
        Assertions.assertEquals(version.getReportsVersion(), 3);
    }

    @Test
    public void shouldParseCorrectly_4() {
        final SchemaVersion version = SchemaVersion.parse("1.2.3.\t");
        Assertions.assertEquals(version.getConceptVersion(), 1);
        Assertions.assertEquals(version.getSamplesVersion(), 2);
        Assertions.assertEquals(version.getReportsVersion(), 3);
    }

    @Test
    public void shouldParseCorrectly_5() {
        final SchemaVersion version = SchemaVersion.parse(" \t 1.2.3. \t ");
        Assertions.assertEquals(version.getConceptVersion(), 1);
        Assertions.assertEquals(version.getSamplesVersion(), 2);
        Assertions.assertEquals(version.getReportsVersion(), 3);
    }

    @Test
    public void shouldNotParse_1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaVersion.parse(null);
        });
    }


    @Test
    public void shouldNotParse_2() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaVersion.parse("1.2");
        });
    }

    @Test
    public void shouldNotParse_3() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaVersion.parse("1.2.3.4");
        });
    }

    @Test
    public void shouldNotParse_4() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaVersion.parse("-1.2.3");
        });
    }

    @Test
    public void shouldNotParse_5() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaVersion.parse("0.0.0");
        });
    }

    @Test
    public void shouldBeEqual() {
        SchemaVersion version_1 = SchemaVersion.parse("1.2.3");
        SchemaVersion version_2 = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(0, version_1.compareTo(version_2));
    }

    @Test
    public void shouldCompare_1() {
        SchemaVersion version_1 = SchemaVersion.parse("1.2.2");
        SchemaVersion version_2 = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(-1, version_1.compareTo(version_2));
    }

    @Test
    public void shouldCompare_2() {
        SchemaVersion version_1 = SchemaVersion.parse("1.2.2");
        SchemaVersion version_2 = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(1, version_2.compareTo(version_1));
    }

    @Test
    public void shouldCompare_3() {
        SchemaVersion version_1 = SchemaVersion.parse("1.1.3");
        SchemaVersion version_2 = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(-1, version_1.compareTo(version_2));
    }

    @Test
    public void shouldCompare_4() {
        SchemaVersion version_1 = SchemaVersion.parse("1.1.3");
        SchemaVersion version_2 = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(1, version_2.compareTo(version_1));
    }

    @Test
    public void shouldCompare_5() {
        SchemaVersion version_1 = SchemaVersion.parse("0.2.3");
        SchemaVersion version_2 = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(-1, version_1.compareTo(version_2));
    }

    @Test
    public void shouldCompare_6() {
        SchemaVersion version_1 = SchemaVersion.parse("0.2.3");
        SchemaVersion version_2 = SchemaVersion.parse("1.2.3");
        Assertions.assertEquals(1, version_2.compareTo(version_1));
    }
}