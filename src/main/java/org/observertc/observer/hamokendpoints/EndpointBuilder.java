package org.observertc.observer.hamokendpoints;

import io.github.balazskreith.hamok.transports.Endpoint;
import org.observertc.observer.configbuilders.Builder;

public interface EndpointBuilder extends Builder<Endpoint> {

    void setBuildingEssentials(BuildersEssentials essentials);
}
