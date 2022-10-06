package org.observertc.observer.hamokdiscovery;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class DiscoveryBuilderConfig {

    @NotNull
    public String type;

    @NotNull
    public Map<String, Object> config;
}
