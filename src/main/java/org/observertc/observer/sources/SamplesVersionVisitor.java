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
    String VERSION_204 = "2.0.4";
    String VERSION_203 = "2.0.3";
    String VERSION_202 = "2.0.2";
    String VERSION_201 = "2.0.1";
    String VERSION_200 = org.observertc.schemas.v200.samples.Samples.VERSION;
    String VERSION_200_BETA_65 = "2.0.0-beta.65";
    String VERSION_200_BETA_64 = "2.0.0-beta.64";
    String VERSION_200_BETA_63 = "2.0.0-beta.63";
    String VERSION_200_BETA_62 = "2.0.0-beta.62";
    String VERSION_200_BETA_61 = "2.0.0-beta.61";
    String VERSION_200_BETA_60 = "2.0.0-beta.60";
    String VERSION_200_BETA_59 = org.observertc.schemas.v200beta59.samples.Samples.VERSION;

    static List<String> getSupportedVersions() {
        return Arrays.asList(
                LATEST_VERSION,
                VERSION_204,
                VERSION_203,
                VERSION_202,
                VERSION_201,
                VERSION_200,
                VERSION_200_BETA_65,
                VERSION_200_BETA_64,
                VERSION_200_BETA_63,
                VERSION_200_BETA_62,
                VERSION_200_BETA_61,
                VERSION_200_BETA_60,
                VERSION_200_BETA_59
        );
    }

    static <TIn, TOut> SamplesVersionVisitor<TIn, TOut> createFunctionalVisitor(
            Function<TIn, TOut> latestVisitor,
            Function<TIn, TOut> v200Visitor,
            Function<TIn, TOut> v200beta59Visitor,
            Function<TIn, TOut> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<TIn, TOut>() {
            @Override
            public TOut visitLatest(TIn tIn) {
                return latestVisitor.apply(tIn);
            }

            @Override
            public TOut visitV200(TIn tIn) {
                return v200Visitor.apply(tIn);
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
            Supplier<TOut> v200Visitor,
            Supplier<TOut> v200beta59Visitor,
            Supplier<TOut> notRecognizedVisitor
    ) {
        return new SamplesVersionVisitor<Void, TOut>() {
            @Override
            public TOut visitLatest(Void tIn) {
                return latestVisitor.get();
            }

            @Override
            public TOut visitV200(Void tIn) {
                return v200Visitor.get();
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
            Consumer<TIn> v200Visitor,
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
            public Void visitV200(TIn tIn) {
                v200Visitor.accept(tIn);
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
            case VERSION_204:
            case VERSION_203:
            case VERSION_202:
            case VERSION_201:
                return visitLatest(obj);
            case  VERSION_200:
            case  VERSION_200_BETA_65:
            case  VERSION_200_BETA_64:
            case  VERSION_200_BETA_63:
            case  VERSION_200_BETA_62:
            case  VERSION_200_BETA_61:
            case  VERSION_200_BETA_60:
                return visitV200(obj);
            case VERSION_200_BETA_59:
                return visitV200beta59(obj);
            default:
                return notRecognized(obj);
        }
    }

    TOut visitLatest(TObj obj);
    TOut visitV200(TObj obj);
    TOut visitV200beta59(TObj obj);
    TOut notRecognized(TObj obj);

}
