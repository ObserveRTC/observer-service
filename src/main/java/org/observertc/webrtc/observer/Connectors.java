package org.observertc.webrtc.observer;

import io.reactivex.rxjava3.annotations.NonNull;
import org.observertc.webrtc.observer.connector.Connector;
import org.observertc.webrtc.observer.connector.ConnectorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class Connectors {
    private static final Logger logger = LoggerFactory.getLogger(Connectors.class);
    public static final String CONNECTOR_CONFIG_FILES_SYSTEM_ENV = "CONNECTOR_CONFIG_FILES";
    private final List<ConnectorBuilder> builders = new ArrayList<>();

    @Inject
    Provider<ConnectorBuilder> connectorBuilderProvider;

    public Connectors(ObserverConfig config) {
        if (Objects.nonNull(config.connectors)) {
            this.addAll(config.connectors);
        }
    }

    @PostConstruct
    void setup() {

    }

    public Connectors addAll(@NonNull List<Map<String, Object>> configs) {
        if (configs.size() < 1) {
            return this;
        }
        for (Map<String, Object> config : configs) {
            this.add(config);
        }
        return this;
    }

    public Connectors add(Map<String, Object> config) {
        ConnectorBuilder builder = new ConnectorBuilder();
        builder.withConfiguration(config);
        this.builders.add(builder);
        return this;
    }

    public List<Connector> getConnectors() {
        List<Connector> result = new LinkedList<>();
        for (ConnectorBuilder builder : this.builders) {
            Optional<Connector> connectorHolder = builder.build();
            if (!connectorHolder.isPresent()) {
                continue;
            }
            Connector connector = connectorHolder.get();
            result.add(connector);
        }
        return result;
    }
}
