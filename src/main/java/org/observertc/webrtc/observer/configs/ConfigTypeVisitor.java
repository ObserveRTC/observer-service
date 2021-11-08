package org.observertc.webrtc.observer.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ConfigTypeVisitor<TIn, TOut> extends BiFunction<TIn, ConfigType, TOut> {
    static final Logger logger = LoggerFactory.getLogger(ConfigTypeVisitor.class);

    static<RIn, ROut> ConfigTypeVisitor<RIn, ROut> makeFunctionalVisitor(
            Function<RIn, ROut> observerReportFunc
    ) {
        return new ConfigTypeVisitor<RIn, ROut>() {
            @Override
            public ROut visitObserverReportConfig(RIn obj) {
                return observerReportFunc.apply(obj);
            }
        };
    }

    static<RIn> ConfigTypeVisitor<RIn, Void> makeConsumerVisitor(
            Consumer<RIn> observerReportConsumer
    ) {
        return new ConfigTypeVisitor<RIn, Void>() {
            @Override
            public Void visitObserverReportConfig(RIn obj) {
                observerReportConsumer.accept(obj);
                return null;
            }
        };
    }

    static ConfigTypeVisitor<Void, Void> makeRunnableVisitor(
            Runnable observerReportCallback
    ) {
        return new ConfigTypeVisitor<Void, Void>() {
            @Override
            public Void visitObserverReportConfig(Void obj) {
                observerReportCallback.run();
                return null;
            }
        };
    }

    @Override
    default TOut apply(TIn obj, ConfigType configType) {
        switch (configType) {
            case OBSERVER_REPORT:
                return this.visitObserverReportConfig(obj);
            default:
                logger.warn("Unrecognized config type {} for visiting", configType);
        }
        return null;
    }

    TOut visitObserverReportConfig(TIn obj);
}
