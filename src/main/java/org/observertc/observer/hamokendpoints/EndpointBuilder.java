package org.observertc.observer.hamokendpoints;


import org.observertc.observer.configbuilders.Builder;

public interface EndpointBuilder extends Builder<HamokEndpoint> {

    void setBuildingEssentials(EndpointsBuildersEssentials essentials);
}
