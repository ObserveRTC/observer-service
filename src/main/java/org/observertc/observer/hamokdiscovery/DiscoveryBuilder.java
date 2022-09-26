package org.observertc.observer.hamokdiscovery;


import org.observertc.observer.configbuilders.Builder;

public interface DiscoveryBuilder extends Builder<RemotePeerDiscovery> {

    void setBuildingEssentials(DiscoveryBuildersEssentials essentials);
}
