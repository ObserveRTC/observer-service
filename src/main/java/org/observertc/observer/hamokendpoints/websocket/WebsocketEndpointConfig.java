
package org.observertc.observer.hamokendpoints.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebsocketEndpointConfig {

    public String serverHost = null;

    @NotNull
    public int serverPort = 5601;

    public int maxMessageSize = 5000;

    public boolean addConnection = false;

}
