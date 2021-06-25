package org.observertc.webrtc.observer.configs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObserverReportConfig {

    @JsonProperty("report-no-ssrc")
    public boolean reportNoSSRC = false;

}
