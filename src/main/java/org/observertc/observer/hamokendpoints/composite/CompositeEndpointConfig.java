package org.observertc.observer.hamokendpoints.composite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompositeEndpointConfig {

    @NotNull
    public int unicastListeningPort = 5601;

    @NotNull
    public int unicastSendingPort = 5602;

    @NotNull
    public int multicastPort = 5600;

    @NotNull
    public String multicastAddress = "225.1.1.1";

    public String context = "Composite Endpoint";

}
