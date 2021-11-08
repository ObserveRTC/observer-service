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

    static Stream<SfuSample.SfuOutboundRtpPad> streamOutboundRtpPads(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.outboundRtpStreams)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.outboundRtpStreams);
    }

    static Stream<SfuSample.SfuInboundRtpPad> streamInboundRtpPads(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.inboundRtpStreams)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.inboundRtpStreams);
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
        streamOutboundRtpPads(sfuSample).forEach(sfuRtpSource -> this.visitSfuOutboundRtpPads(obj, sfuId, sfuRtpSource));
        streamInboundRtpPads(sfuSample).forEach(sfuRtpSink -> this.visitSfuInboundRtpPads(obj, sfuId, sfuRtpSink));
        streamSctpStreams(sfuSample).forEach(sctpStream -> this.visitSctpStream(obj, sfuId, sctpStream));
    }


    void visitSfuTransport(T obj, UUID sfuId, SfuSample.SfuTransport sfuTransport);

    void visitSfuOutboundRtpPads(T obj, UUID sfuId, SfuSample.SfuOutboundRtpPad sfuRtpSource);

    void visitSfuInboundRtpPads(T obj, UUID sfuId, SfuSample.SfuInboundRtpPad sfuRtpSink);

    void visitSctpStream(T obj, UUID sfuId, SfuSample.SctpStream sctpStream);

}
