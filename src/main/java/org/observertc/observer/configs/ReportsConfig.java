package org.observertc.observer.configs;

public class ReportsConfig {
//    public boolean sendObserverEvents = true;
//    public boolean sendCallEvents = true;
//    public boolean sendCallMeta = true;
//    public boolean sendClientExtensions = true;
//    public boolean sendInboundAudioTracks = true;
//    public boolean sendInboundVideoTracks = true;
//    public boolean sendOutboundAudioTracks = true;
//    public boolean sendOutboundVideoTracks = true;
//    public boolean sendClientTransports = true;
//    public boolean sendClientDataChannels = true;
//
//    public boolean sendSfuEvents = true;
//    public boolean sendSfuMeta = true;
//    public boolean sendSfuTransports = true;
//    public boolean sendSfuSctpStreams = true;
//
//    public boolean sendSfuInboundRtpStreams = true;
//    public boolean sendSfuOutboundRtpStreams = true;
//
//    public boolean sendSfuExtensions = true;

    public boolean sendObserverEvents = false;
    public boolean sendCallEvents = false;
    public boolean sendCallMeta = false;
    public boolean sendClientExtensions = false;
    public boolean sendInboundAudioTracks = false;
    public boolean sendInboundVideoTracks = false;
    public boolean sendOutboundAudioTracks = false;
    public boolean sendOutboundVideoTracks = false;
    public boolean sendClientTransports = false;
    public boolean sendClientDataChannels = false;

    public boolean sendSfuEvents = false;
    public boolean sendSfuMeta = false;
    public boolean sendSfuTransports = false;
    public boolean sendSfuSctpStreams = false;

    public boolean sendSfuInboundRtpStreams = false;
    public boolean sendSfuOutboundRtpStreams = false;

    public boolean sendSfuExtensions = false;

    public static ReportsConfig createNegated() {
        var result = new ReportsConfig();
        result.sendObserverEvents = true;
        result.sendCallEvents = true;
        result.sendCallMeta = true;
        result.sendClientExtensions = true;
        result.sendInboundAudioTracks = true;
        result.sendInboundVideoTracks = true;
        result.sendOutboundAudioTracks = true;
        result.sendOutboundVideoTracks = true;
        result.sendClientTransports = true;
        result.sendClientDataChannels = true;

        result.sendSfuEvents = true;
        result.sendSfuMeta = true;
        result.sendSfuTransports = true;
        result.sendSfuSctpStreams = true;

        result.sendSfuInboundRtpStreams = true;
        result.sendSfuOutboundRtpStreams = true;
        result.sendSfuExtensions = true;

        return result;
    }
}
