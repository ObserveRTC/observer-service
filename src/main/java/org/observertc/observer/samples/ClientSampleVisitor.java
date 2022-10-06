package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples.ClientSample;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface ClientSampleVisitor<T> extends BiConsumer<T, ClientSample> {

    static Stream<ClientSample.Certificate> streamCertificates(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.certificates)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.certificates);
    }

    static Stream<ClientSample.MediaCodecStats> streamCodecs(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.codecs)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.codecs);
    }

    static Stream<ClientSample.DataChannel> streamDataChannels(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.dataChannels)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.dataChannels);
    }

    static Stream<ClientSample.ExtensionStat> streamExtensionStats(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.extensionStats)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.extensionStats);
    }

    static Stream<ClientSample.IceLocalCandidate> streamIceLocalCandidates(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.iceLocalCandidates)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.iceLocalCandidates);
    }

    static Stream<ClientSample.IceRemoteCandidate> streamIceRemoteCandidates(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.iceRemoteCandidates)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.iceRemoteCandidates);
    }

    static Stream<String> streamIceServers(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.iceServers)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.iceServers);
    }

    static Stream<ClientSample.InboundAudioTrack> streamInboundAudioTracks(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.inboundAudioTracks)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.inboundAudioTracks);
    }

    static Stream<ClientSample.InboundVideoTrack> streamInboundVideoTracks(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.inboundVideoTracks)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.inboundVideoTracks);
    }

    static Stream<ClientSample.OutboundAudioTrack> streamOutboundAudioTracks(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.outboundAudioTracks)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.outboundAudioTracks);
    }

    static Stream<ClientSample.OutboundVideoTrack> streamOutboundVideoTracks(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.outboundVideoTracks)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.outboundVideoTracks);
    }

    static Stream<String> streamMediaConstraints(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.mediaConstraints)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.mediaConstraints);
    }


    static Stream<ClientSample.MediaDevice> streamMediaDevices(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.mediaDevices)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.mediaDevices);
    }

    static Stream<ClientSample.MediaSourceStat> streamMediaSources(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.mediaSources)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.mediaSources);
    }


    static Stream<ClientSample.PeerConnectionTransport> streamPeerConnectionTransports(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.pcTransports)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.pcTransports);
    }

    static Stream<ClientSample.IceCandidatePair> streamIceCandidatePairs(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.iceCandidatePairs)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.iceCandidatePairs);
    }

    static Stream<String> streamUserMediaErrors(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.userMediaErrors)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.userMediaErrors);
    }

    static Stream<String> streamLocalSDP(@NotNull ClientSample clientSample) {
        if (Objects.isNull(clientSample.localSDPs)) {
            return Stream.empty();
        }
        return Arrays.stream(clientSample.localSDPs);
    }



    @Override
    default void accept(T obj, ClientSample clientSample) {
        if (Objects.isNull(clientSample)) {
            return;
        }
        String clientId = clientSample.clientId;
        streamCertificates(clientSample).forEach(certificate -> this.visitCertificate(obj, clientId, certificate));
        streamCodecs(clientSample).forEach(codec -> this.visitCodec(obj, clientId, codec));
        streamDataChannels(clientSample).forEach(dataChannel -> this.visitDataChannel(obj, clientId, dataChannel));
        streamExtensionStats(clientSample).forEach(extensionStat -> this.visitExtensionStat(obj, clientId, extensionStat));
        streamIceLocalCandidates(clientSample).forEach(iceLocalCandidate -> this.visitIceLocalCandidate(obj, clientId, iceLocalCandidate));
        streamIceRemoteCandidates(clientSample).forEach(iceRemoteCandidate -> this.visitIceRemoteCandidate(obj, clientId, iceRemoteCandidate));
        streamIceServers(clientSample).forEach(iceServer -> this.visitIceServer(obj, clientId, iceServer));
        streamInboundAudioTracks(clientSample).forEach(inboundAudioTrack -> this.visitInboundAudioTrack(obj, clientId, inboundAudioTrack));
        streamInboundVideoTracks(clientSample).forEach(inboundVideoTrack -> this.visitInboundVideoTrack(obj, clientId, inboundVideoTrack));
        streamOutboundAudioTracks(clientSample).forEach(outboundAudioTrack -> this.visitOutboundAudioTrack(obj, clientId, outboundAudioTrack));
        streamOutboundVideoTracks(clientSample).forEach(outboundVideoTrack -> this.visitOutboundVideoTrack(obj, clientId, outboundVideoTrack));
        streamMediaConstraints(clientSample).forEach(mediaConstraint -> this.visitMediaConstraint(obj, clientId, mediaConstraint));
        streamMediaDevices(clientSample).forEach(mediaDevice -> this.visitMediaDevice(obj, clientId, mediaDevice));
        streamMediaSources(clientSample).forEach(mediaSource -> this.visitMediaSource(obj, clientId, mediaSource));
        streamPeerConnectionTransports(clientSample).forEach(pcTransport -> this.visitPeerConnectionTransport(obj, clientId, pcTransport));
        streamUserMediaErrors(clientSample).forEach(userMediaError -> this.visitUserMediaError(obj, clientId, userMediaError));
        streamLocalSDP(clientSample).forEach(userMediaError -> this.visitUserMediaError(obj, clientId, userMediaError));
    }



    void visitCertificate(T obj, String clientId, ClientSample.Certificate certificate);

    void visitCodec(T obj, String clientId, ClientSample.MediaCodecStats codec);

    void visitDataChannel(T obj, String clientId, ClientSample.DataChannel dataChannel);

    void visitExtensionStat(T obj, String clientId, ClientSample.ExtensionStat extensionStat);

    void visitIceLocalCandidate(T obj, String clientId, ClientSample.IceLocalCandidate iceLocalCandidate);

    void visitIceRemoteCandidate(T obj, String clientId, ClientSample.IceRemoteCandidate iceRemoteCandidate);

    void visitIceServer(T obj, String clientId, String iceServer);

    void visitInboundAudioTrack(T obj, String clientId, ClientSample.InboundAudioTrack inboundAudioTrack);

    void visitInboundVideoTrack(T obj, String clientId, ClientSample.InboundVideoTrack inboundVideoTrack);

    void visitOutboundAudioTrack(T obj, String clientId, ClientSample.OutboundAudioTrack outboundAudioTrack);

    void visitOutboundVideoTrack(T obj, String clientId, ClientSample.OutboundVideoTrack outboundVideoTrack);

    void visitMediaConstraint(T obj, String clientId, String mediaConstraint);

    void visitMediaDevice(T obj, String clientId, ClientSample.MediaDevice mediaDevice);

    void visitMediaSource(T obj, String clientId, ClientSample.MediaSourceStat mediaSource);

    void visitPeerConnectionTransport(T obj, String clientId, ClientSample.PeerConnectionTransport pcTransport);

    void visitUserMediaError(T obj, String clientId, String userMediaError);

    void visitLocalSdp(T obj, String clientId, String userMediaError);

}
