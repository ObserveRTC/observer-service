package org.observertc.webrtc.observer.samples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * A compound object holds a set of measurements belonging to a aspecific time
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public  class Samples {

    /**
     * array of client samples
     */
    @JsonProperty("clientSamples")
    public ClientSample[] clientSamples;

    /**
     * array of sfu samples
     */
    @JsonProperty("sfuSamples")
    public SfuSample[] sfuSamples;

//    public enum Flags {
//        JSON_DELTA,
//        JSZIP,
//    }
}