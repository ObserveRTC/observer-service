package org.observertc.observer.sources;

import org.observertc.schemas.samples.Samples;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface SamplesVersionVisitor<TObj, TOut> extends BiFunction<TObj, String, TOut> {

    static <TIn, TOut> SamplesVersionVisitor<TIn, TOut> createFunctionalVisitor(
            Function<TIn, TOut> latestVisitor,
            Function<TIn, TOut> v200beta59Visitor,
            Function<TIn, TOut> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<TIn, TOut>() {
            @Override
            public TOut visitLatest(TIn tIn) {
                return latestVisitor.apply(tIn);
            }

            @Override
            public TOut visitV200beta59(TIn tIn) {
                return v200beta59Visitor.apply(tIn);
            }

            @Override
            public TOut notRecognized(TIn tIn) {
                return notRecognizedVisitor.apply(tIn);
            }
        };
    }

    static <TOut> SamplesVersionVisitor<Void, TOut> createSupplierVisitor(
            Supplier<TOut> latestVisitor,
            Supplier<TOut> v200beta59Visitor,
            Supplier<TOut> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<Void, TOut>() {
            @Override
            public TOut visitLatest(Void tIn) {
                return latestVisitor.get();
            }

            @Override
            public TOut visitV200beta59(Void tIn) {
                return v200beta59Visitor.get();
            }

            @Override
            public TOut notRecognized(Void tIn) {
                return notRecognizedVisitor.get();
            }
        };
    }

    static <TIn> SamplesVersionVisitor<TIn, Void> visitByConsumers(
            Consumer<TIn> latestVisitor,
            Consumer<TIn> v200beta59Visitor,
            Consumer<TIn> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<TIn, Void>() {
            @Override
            public Void visitLatest(TIn tIn) {
                latestVisitor.accept(tIn);
                return null;
            }

            @Override
            public Void visitV200beta59(TIn tIn) {
                v200beta59Visitor.accept(tIn);
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
            case "latest":
            case Samples.VERSION:
            case  "2.0.0-beta.63":
            case  "2.0.0-beta.62":
            case  "2.0.0-beta.61":
            case  "2.0.0-beta.60":
                return visitLatest(obj);
            case org.observertc.schemas.v200beta59.samples.Samples.VERSION:
                return visitV200beta59(obj);
            default:
                return notRecognized(obj);
        }
    }

    TOut visitLatest(TObj obj);
    TOut visitV200beta59(TObj obj);
    TOut notRecognized(TObj obj);

}
