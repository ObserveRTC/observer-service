package org.observertc.observer.hamokdiscovery.statics;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class StaticDiscoveryConfig {

    @NotNull
    public Map<String, Object> peers;


    public static class StaticDiscoveryConfigRemotePeer {
        @Min(1024)
        public int port;

        @NotNull
        public String host;
    }
}
