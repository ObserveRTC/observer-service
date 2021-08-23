package org.observertc.webrtc.observer.samples;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface SfuSampleVisitor<T> extends BiConsumer<T, SfuSample> {

    static Stream<SfuSample.SfuTransport> streamTransports(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.sfuTransports)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.sfuTransports);
    }

    static Stream<SfuSample.SfuRtpSource> streamRtpSources(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.rtpSources)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.rtpSources);
    }

    static Stream<SfuSample.SfuRtpSink> streamRtpSinks(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.rtpSinks)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.rtpSinks);
    }

    static Stream<SfuSample.SctpStream> streamSctpStreams(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.sctpStreams)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.sctpStreams);
    }


    @Override
    default void accept(T obj, SfuSample sfuSample) {
        if (Objects.isNull(sfuSample)) {
            return;
        }
        UUID sfuId = UUID.fromString(sfuSample.sfuId);
        streamTransports(sfuSample).forEach(transport -> this.visitSfuTransport(obj, sfuId, transport));
        streamRtpSources(sfuSample).forEach(sfuRtpSource -> this.visitSfuRtpSource(obj, sfuId, sfuRtpSource));
        streamRtpSinks(sfuSample).forEach(sfuRtpSink -> this.visitSfuRtpSink(obj, sfuId, sfuRtpSink));
        streamSctpStreams(sfuSample).forEach(sctpStream -> this.visitSctpStream(obj, sfuId, sctpStream));
    }


    void visitSfuTransport(T obj, UUID sfuId, SfuSample.SfuTransport sfuTransport);

    void visitSfuRtpSource(T obj, UUID sfuId, SfuSample.SfuRtpSource sfuRtpSource);

    void visitSfuRtpSink(T obj, UUID sfuId, SfuSample.SfuRtpSink sfuRtpSink);

    void visitSctpStream(T obj, UUID sfuId, SfuSample.SctpStream sctpStream);

}
