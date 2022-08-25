package org.observertc.observer.repositories.endpoints;

import io.github.balazskreith.hamok.transports.Endpoint;
import org.observertc.observer.configbuilders.Builder;

import java.util.Map;
import java.util.UUID;

public interface EndpointBuilder extends Builder<Endpoint> {

    void setEndpointId(UUID endpointId);

    void setBeans(Map<Class, Object> beans);
}
