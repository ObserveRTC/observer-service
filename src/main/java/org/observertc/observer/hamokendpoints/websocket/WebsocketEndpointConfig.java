
package org.observertc.observer.hamokendpoints.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebsocketEndpointConfig {

    public String serverHost = null;

    @NotNull
    public int serverPort = 5601;

    @NotNull
    public Map<String, Object> discovery;

    public int maxMessageSize = 5000;

}
