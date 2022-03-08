package org.observertc.observer.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MuxedReport {
    /**
     * a Unique generated id for the sfu samples are originated from
     */
    @JsonProperty("type")
    public String type;

    @JsonProperty("payload")
    public byte[] payload;
}
