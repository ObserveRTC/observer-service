package org.observertc.observer.hamokendpoints.websockets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WsEndpointConfig {

    @NotNull
    public String localhost = "localhost";

    @NotNull
    public String remoteHost = "localhost";

    @NotNull
    public int unicastListeningPort = 5601;

    @NotNull
    public int unicastSendingPort = 5602;

}
