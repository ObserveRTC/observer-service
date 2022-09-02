package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples.SfuSample;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface SfuSampleVisitor<T> extends BiConsumer<T, SfuSample> {

    static Stream<SfuSample.SfuTransport> streamTransports(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.transports)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.transports);
    }

    static Stream<SfuSample.SfuOutboundRtpPad> streamOutboundRtpPads(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.outboundRtpPads)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.outboundRtpPads);
    }

    static Stream<SfuSample.SfuInboundRtpPad> streamInboundRtpPads(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.inboundRtpPads)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.inboundRtpPads);
    }

    static Stream<SfuSample.SfuSctpChannel> streamSctpStreams(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.sctpChannels)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.sctpChannels);
    }

    static Stream<SfuSample.SfuExtensionStats> streamExtensionStats(@NotNull SfuSample sfuSample) {
        if (Objects.isNull(sfuSample.extensionStats)) {
            return Stream.empty();
        }
        return Arrays.stream(sfuSample.extensionStats);
    }


    @Override
    default void accept(T obj, SfuSample sfuSample) {
        if (Objects.isNull(sfuSample)) {
            return;
        }
        String sfuId = sfuSample.sfuId;
        streamTransports(sfuSample).forEach(transport -> this.visitSfuTransport(obj, sfuId, transport));
        streamOutboundRtpPads(sfuSample).forEach(sfuRtpSource -> this.visitSfuOutboundRtpPads(obj, sfuId, sfuRtpSource));
        streamInboundRtpPads(sfuSample).forEach(sfuRtpSink -> this.visitSfuInboundRtpPads(obj, sfuId, sfuRtpSink));
        streamSctpStreams(sfuSample).forEach(sctpStream -> this.visitSctpStream(obj, sfuId, sctpStream));
    }


    void visitSfuTransport(T obj, String sfuId, SfuSample.SfuTransport sfuTransport);

    void visitSfuOutboundRtpPads(T obj, String sfuId, SfuSample.SfuOutboundRtpPad sfuRtpSource);

    void visitSfuInboundRtpPads(T obj, String sfuId, SfuSample.SfuInboundRtpPad sfuRtpSink);

    void visitSctpStream(T obj, String sfuId, SfuSample.SfuSctpChannel sctpStream);

}
