package org.observertc.webrtc.observer.security;

import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ObfuscationTypeVisitor<TIn, TOut> extends BiFunction<TIn, ObserverConfig.ObfuscationsConfig.ObfuscationType, TOut> {
    static final Logger logger = LoggerFactory.getLogger(ObfuscationTypeVisitor.class);

    static<RIn, ROut> ObfuscationTypeVisitor<RIn, ROut> makeFunctionalVisitor(
            Function<RIn, ROut> anonymizationFunction,
            Function<RIn, ROut> noneFunction
    ) {
        return new ObfuscationTypeVisitor<RIn, ROut>() {
            @Override
            public ROut visitAnonymizationObfuscationType(RIn obj) {
                return anonymizationFunction.apply(obj);
            }

            @Override
            public ROut visitNoneObfuscationType(RIn obj) {
                return noneFunction.apply(obj);
            }
        };
    }

    static<RIn> ObfuscationTypeVisitor<RIn, Void> makeConsumerVisitor(
            Consumer<RIn> anonymizationConsumer,
            Consumer<RIn> noneConsumer
    ) {
        return new ObfuscationTypeVisitor<RIn, Void>() {
            @Override
            public Void visitAnonymizationObfuscationType(RIn obj) {
                anonymizationConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitNoneObfuscationType(RIn obj) {
                noneConsumer.accept(obj);
                return null;
            }
        };
    }

    static<ROut> ObfuscationTypeVisitor<Void, ROut> makeSupplierVisitor(
            Supplier<ROut> anonymizationSupplier,
            Supplier<ROut> noneSupplier
    ) {
        return new ObfuscationTypeVisitor<Void, ROut>() {
            @Override
            public ROut visitAnonymizationObfuscationType(Void obj) {
                return anonymizationSupplier.get();
            }

            @Override
            public ROut visitNoneObfuscationType(Void obj) {
                return noneSupplier.get();
            }
        };
    }

    static ObfuscationTypeVisitor<Void, Void> makeRunnableVisitor(
            Runnable anonymizationConsumer,
            Runnable noneConsumer
    ) {
        return new ObfuscationTypeVisitor<Void, Void>() {
            @Override
            public Void visitAnonymizationObfuscationType(Void obj) {
                anonymizationConsumer.run();
                return null;
            }

            @Override
            public Void visitNoneObfuscationType(Void obj) {
                noneConsumer.run();
                return null;
            }
        };
    }

    @Override
    default TOut apply(TIn obj, ObserverConfig.ObfuscationsConfig.ObfuscationType obfuscationType) {
        switch (obfuscationType) {
            case ANONYMIZATION:
                return this.visitAnonymizationObfuscationType(obj);
            case NONE:
                return visitNoneObfuscationType(obj);
            default:
                logger.warn("Unrecognized config type {} for visiting", obfuscationType);
        }
        return null;
    }

    TOut visitAnonymizationObfuscationType(TIn obj);

    TOut visitNoneObfuscationType(TIn obj);
}
