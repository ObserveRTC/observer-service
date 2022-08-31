package org.observertc.observer.repositories.endpoints;

import io.github.balazskreith.hamok.transports.Endpoint;
import org.observertc.observer.configbuilders.Builder;

public interface EndpointBuilder extends Builder<Endpoint> {

    void setBuildingEssentials(BuildersEssentials essentials);
}
