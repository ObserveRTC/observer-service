package org.observertc.observer.sources;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface SamplesVersionVisitor<TObj, TOut> extends BiFunction<TObj, String, TOut> {

    String LATEST_VERSION = "latest";
    String VERSION_220 = "2.2.0";
    String VERSION_219 = "2.1.9";
    String VERSION_218 = "2.1.8";
    String VERSION_217 = "2.1.7";
    String VERSION_216 = "2.1.6";
    String VERSION_215 = "2.1.5";
    String VERSION_214 = "2.1.4";
    String VERSION_213 = "2.1.3";
    String VERSION_212 = "2.1.2";
    String VERSION_211 = "2.1.1";
    String VERSION_210 = "2.1.0";

    static List<String> getSupportedVersions() {
        return Arrays.asList(
                LATEST_VERSION,
                VERSION_220,
                VERSION_219,
                VERSION_218,
                VERSION_217,
                VERSION_216,
                VERSION_215,
                VERSION_214,
                VERSION_213,
                VERSION_212,
                VERSION_211,
                VERSION_210
        );
    }

    static <TIn, TOut> SamplesVersionVisitor<TIn, TOut> createFunctionalVisitor(
            Function<TIn, TOut> latestVisitor,
            Function<TIn, TOut> visit210,
            Function<TIn, TOut> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<TIn, TOut>() {
            @Override
            public TOut visitLatest(TIn tIn) {
                return latestVisitor.apply(tIn);
            }

            @Override
            public TOut visit210(TIn tIn) {
                return visit210.apply(tIn);
            }


            @Override
            public TOut notRecognized(TIn tIn) {
                return notRecognizedVisitor.apply(tIn);
            }
        };
    }

    static <TOut> SamplesVersionVisitor<Void, TOut> createSupplierVisitor(
            Supplier<TOut> latestVisitor,
            Supplier<TOut> visit210,
            Supplier<TOut> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<Void, TOut>() {
            @Override
            public TOut visitLatest(Void tIn) {
                return latestVisitor.get();
            }

            @Override
            public TOut visit210(Void tIn) {
                return visit210.get();
            }


            @Override
            public TOut notRecognized(Void tIn) {
                return notRecognizedVisitor.get();
            }
        };
    }

    static <TIn> SamplesVersionVisitor<TIn, Void> visitByConsumers(
            Consumer<TIn> latestVisitor,
            Consumer<TIn> visit210,
            Consumer<TIn> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<TIn, Void>() {
            @Override
            public Void visitLatest(TIn tIn) {
                latestVisitor.accept(tIn);
                return null;
            }

            @Override
            public Void visit210(TIn tIn) {
                visit210.accept(tIn);
                return null;
            }

            @Override
            public Void notRecognized(TIn tIn) {
                notRecognizedVisitor.accept(tIn);
                return null;
            }
        };
    }

    static boolean isVersionValid(String version) {
        return SamplesVersionVisitor.<String, Boolean>createFunctionalVisitor(
                input -> true,
                input -> true,
                input -> false
        ).apply(version, version);
    }


    @Override
    default TOut apply(TObj obj, String version) {
        if (version == null) {
            return notRecognized(obj);
        }

        switch (version.toLowerCase(Locale.ROOT)) {
            case LATEST_VERSION:
            case VERSION_220:
                return visitLatest(obj);
            case VERSION_219:
            case VERSION_218:
            case VERSION_217:
            case VERSION_216:
            case VERSION_215:
            case VERSION_214:
            case VERSION_213:
            case VERSION_212:
            case VERSION_211:
            case VERSION_210:
                return visit210(obj);
            default:
                return notRecognized(obj);
        }
    }

    TOut visitLatest(TObj obj);
    TOut visit210(TObj obj);
    TOut notRecognized(TObj obj);

}
