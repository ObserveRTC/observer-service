package org.observertc.observer.sources;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SchemaVersion implements Comparable<SchemaVersion> {

    public static final SchemaVersion VERSION_220 = SchemaVersion.parse("2.2.0");
    public static final SchemaVersion VERSION_219 = SchemaVersion.parse("2.1.9");
    public static final SchemaVersion VERSION_218 = SchemaVersion.parse("2.1.8");
    public static final SchemaVersion VERSION_217 = SchemaVersion.parse("2.1.7");
    public static final SchemaVersion VERSION_216 = SchemaVersion.parse("2.1.6");
    public static final SchemaVersion VERSION_215 = SchemaVersion.parse("2.1.5");
    public static final SchemaVersion VERSION_214 = SchemaVersion.parse("2.1.4");
    public static final SchemaVersion VERSION_213 = SchemaVersion.parse("2.1.3");
    public static final SchemaVersion VERSION_212 = SchemaVersion.parse("2.1.2");
    public static final SchemaVersion VERSION_211 = SchemaVersion.parse("2.1.1");
    public static final SchemaVersion VERSION_210 = SchemaVersion.parse("2.1.0");

    public static List<SchemaVersion> getSupportedVersions() {
        return Arrays.asList(
                VERSION_220,
                VERSION_219,
                VERSION_218,
                VERSION_217,
                VERSION_216,
                VERSION_215,
                VERSION_214,
                VERSION_213,
                VERSION_212,
                VERSION_211,
                VERSION_210
        );
    }

    private static final AtomicReference<String> supportedVersionsList = new AtomicReference<>(null);
    public static String getSupportedVersionsList() {
        if (supportedVersionsList.get() == null) {
            var list = String.join(", ", SchemaVersion.getSupportedVersions().stream().map(SchemaVersion::toString).collect(Collectors.toList()));
            supportedVersionsList.compareAndSet(null, list);
        }
        return supportedVersionsList.get();
    }


    public static SchemaVersion parse(String schemaVersion) {
        if (schemaVersion == null) {
            throw new IllegalArgumentException("Cannot parse a null provided schema version");
        }
        String[] parts = schemaVersion.trim().split("\\.");
        if (parts.length != 3) {
            if (parts.length == 4 && parts[3] == "") {
                // trailing dot, we can parse this
            } else {
                throw new IllegalArgumentException("Schema Version must contain 3 parts: conceptVersion.samplesVersion.reportsVersion No less, no more");
            }
        }
        return new SchemaVersion(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        );
    }

    private final int conceptVersion;
    private final int samplesVersion;
    private final int reportsVersion;

    private SchemaVersion(int conceptVersion, int samplesVersion, int reportsVersion) {
        if (conceptVersion < 0 || samplesVersion < 0 || reportsVersion < 0) {
            throw new IllegalArgumentException("Version numbers must be positive!");
        }
        if (conceptVersion == 0 && samplesVersion == 0 && reportsVersion == 0) {
            throw new IllegalArgumentException("Version 0.0.0 does not exists");
        }
        this.conceptVersion = conceptVersion;
        this.samplesVersion = samplesVersion;
        this.reportsVersion = reportsVersion;
    }

    public int getConceptVersion() {
        return this.conceptVersion;
    }

    public int getReportsVersion() {
        return this.reportsVersion;
    }

    public int getSamplesVersion() {
        return this.samplesVersion;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.conceptVersion)
                .append('.')
                .append(this.samplesVersion)
                .append('.')
                .append(this.reportsVersion);
        return result.toString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SchemaVersion)) {
            return false;
        }
        SchemaVersion ov = (SchemaVersion) other;
        if (ov.conceptVersion != conceptVersion || ov.samplesVersion != samplesVersion || ov.reportsVersion != reportsVersion) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(@NotNull SchemaVersion o) {
        int result = conceptVersion - o.conceptVersion;
        if (result != 0) {
            return result;
        }
        result = samplesVersion - o.samplesVersion;
        if (result != 0) {
            return result;
        }
        result = reportsVersion - o.reportsVersion;
        return result;
    }
}
