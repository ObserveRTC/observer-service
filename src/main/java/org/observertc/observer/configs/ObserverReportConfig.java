package org.observertc.observer.configs;

import com.fasterxml.jackson.annotation.JsonProperty;

@Deprecated
public class ObserverReportConfig {

    @JsonProperty("report-no-ssrc")
    public boolean reportNoSSRC = false;

}
