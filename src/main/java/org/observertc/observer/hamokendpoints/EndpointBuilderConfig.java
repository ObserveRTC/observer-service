package org.observertc.observer.hamokendpoints;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class EndpointBuilderConfig {

    @NotNull
    public String type;

    public Map<String, Object> config;

}
