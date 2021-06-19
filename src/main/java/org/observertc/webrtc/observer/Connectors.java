package org.observertc.webrtc.observer;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.observertc.webrtc.observer.connectors.Connector;
import org.observertc.webrtc.observer.connectors.ConnectorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Singleton
public class Connectors implements Consumer<Report> {
    private static final Logger logger = LoggerFactory.getLogger(Connectors.class);
    private final List<ConnectorBuilder> builders = new ArrayList<>();
    private final AtomicReference<List<Connector>> connectorsHolder = new AtomicReference<>(null);

    @Inject
    Provider<ConnectorBuilder> connectorBuilderProvider;

    @Inject
    ObserverConfigDispatcher configDispatcher;



    public Connectors(ObserverConfig config) {
        if (Objects.nonNull(config.connectors)) {
            this.addAll(config.connectors);
        }
    }

    @PostConstruct
    void setup() {
        var observerConfig = configDispatcher.getConfig();
        this.update(observerConfig);
        this.configDispatcher.onConnectorsChanged()
                .buffer(10, TimeUnit.SECONDS)
                .filter(buf -> Objects.nonNull(buf) && 0 < buf.size())
                // last item... hopefully
                .map(buf -> buf.get(buf.size() - 1))
                .map(event -> event.config)
                .subscribe(this::update);
    }

    @Override
    public void accept(Report report) {
        Subject<Report> subject;

        var connectors = this.connectorsHolder.get();
        for (var connector : connectors) {
            connector.onNext(report);
        }
    }

    private void update(ObserverConfig observerConfig) {
        var configs = observerConfig.connectors;
        var connectors = this.makeConnectors(configs);
        if (Objects.isNull(connectors)) {
            logger.warn("NullConnectors was built.");
            return;
        }

        if (connectors.size() < 1) {
            logger.warn("No Connectors were specified, no Reports is generated from now on");
            this.connectorsHolder.get().stream().forEach(Connector::onComplete);
            this.connectorsHolder.set(Collections.EMPTY_LIST);
            return;
        }

        var deprecated = this.connectorsHolder.get();

        connectors.stream().forEach(connector -> {
            connector.onSubscribe(null);
        });

        this.connectorsHolder.set(connectors);

        if (Objects.isNull(deprecated) || deprecated.size() < 1) {
            return;
        }
        deprecated.stream().forEach(connector -> {
            connector.onComplete();
        });
    }

    private List<Connector> makeConnectors(List<Map<String, Object>> configs) {
        List<Connector> result = configs.stream()
                .map(config -> {
                    ConnectorBuilder builder = new ConnectorBuilder();
                    builder.withConfiguration(config);
                    return builder;
                })
                .map(builder -> {
                    try {
                        return builder.build();
                    } catch (Throwable t) {
                        logger.warn("There is an error occurred during building", t);
                        return Optional.<Connector>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return result;
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
