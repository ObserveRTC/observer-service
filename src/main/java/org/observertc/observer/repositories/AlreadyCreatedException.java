package org.observertc.observer.repositories;

public class AlreadyCreatedException extends RuntimeException {
    static AlreadyCreatedException wrapCallId(String callId) {
        var message = String.format("Call %s is already created or marked to be created", callId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapClientId(String clientId) {
        var message = String.format("Client %s is already created or marked to be created", clientId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapPeerConnectionId(String peerConnectionId) {
        var message = String.format("PeerConnection %s is already created or marked to be created", peerConnectionId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapInboundAudioTrack(String trackId) {
        var message = String.format("InboundAudioTrack %s is already created or marked to be created", trackId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapInboundVideoTrack(String trackId) {
        var message = String.format("InboundAudioTrack %s is already created or marked to be created", trackId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapOutboundAudioTrack(String trackId) {
        var message = String.format("OutboundAudioTrack %s is already created or marked to be created", trackId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapOutboundVideoTrack(String trackId) {
        var message = String.format("OutboundVideoTrack %s is already created or marked to be created", trackId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapSfuMediaSink(String sinkId) {
        var message = String.format("Sfu Media Sink %s is already created or marked to be created", sinkId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapSfuMediaStream(String streamId) {
        var message = String.format("Sfu Media Stream %s is already created or marked to be created", streamId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapSfuId(String sfuId) {
        var message = String.format("SFU %s is already created or marked to be created", sfuId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapSfuTransport(String sfuTransportId) {
        var message = String.format("SFU Transport %s is already created or marked to be created", sfuTransportId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapSfuInboundRtpPad(String sfuRtpPadId) {
        var message = String.format("SFU Inbound RTP pad %s is already created or marked to be created", sfuRtpPadId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapSfuOutboundRtpPad(String sfuRtpPadId) {
        var message = String.format("SFU Outbound RTP pad %s is already created or marked to be created", sfuRtpPadId);
        return new AlreadyCreatedException(message);
    }

    static AlreadyCreatedException wrapSfuSctpStream(String sfuRtpPadId) {
        var message = String.format("SFU Sctp Stream %s is already created or marked to be created", sfuRtpPadId);
        return new AlreadyCreatedException(message);
    }

    public AlreadyCreatedException(String message) {
        super(message);
    }
}
