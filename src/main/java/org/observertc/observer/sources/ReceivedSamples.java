package org.observertc.observer.sources;

import org.observertc.schemas.samples.Samples;

public class ReceivedSamples {
    public static ReceivedSamples of(String serviceId, String mediaUnitId, Samples samples) {
        var result = new ReceivedSamples();
        result.serviceId = serviceId;
        result.mediaUnitId = mediaUnitId;
        result.samples = samples;
        return result;
    }
    public String serviceId;
    public String mediaUnitId;
    public Samples samples;

    private ReceivedSamples() {

    }
}
