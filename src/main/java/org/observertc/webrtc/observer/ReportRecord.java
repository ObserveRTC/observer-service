package org.observertc.webrtc.observer;

import org.observertc.webrtc.schemas.reports.Report;

import java.util.Objects;
import java.util.UUID;

public class ReportRecord {

    public static ReportRecord of(UUID key, Report value) {
        return new ReportRecord(key, value);
    }

    public final UUID key;
    public final Report value;

    public ReportRecord(UUID key, Report value) {
        if (Objects.isNull(key) || Objects.isNull(value)) {
            throw new IllegalStateException("Neither key ()" + key + "nor the value is allowed to be null");
        }
        this.key = key;
        this.value = value;
    }
}
