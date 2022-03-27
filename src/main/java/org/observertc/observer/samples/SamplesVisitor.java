package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples;
import org.observertc.schemas.samples.Samples.ClientSample;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface SamplesVisitor<T> extends BiConsumer<T, Samples> {

    static Stream<ClientSample> streamClientSamples(@NotNull Samples samples) {
        if (Objects.isNull(samples.clientSamples)) {
            return Stream.empty();
        }
        return Arrays.stream(samples.clientSamples);
    }

    static Stream<Samples.SfuSample> streamSfuSamples(@NotNull Samples samples) {
        if (Objects.isNull(samples.sfuSamples)) {
            return Stream.empty();
        }
        return Arrays.stream(samples.sfuSamples);
    }



    @Override
    default void accept(T obj, Samples samples) {
        if (Objects.isNull(samples)) {
            return;
        }
        streamClientSamples(samples).forEach(clientSample -> this.visitClientSample(obj, clientSample));
        streamSfuSamples(samples).forEach(sfuSample -> this.visitSfuSample(obj, sfuSample));
    }

    void visitClientSample(T obj, ClientSample clientSample);

    void visitSfuSample(T obj, Samples.SfuSample sfuSample);

}
