package org.observertc.observer.configs;

public class ReportsConfig {
    public boolean sendObserverEvents = true;
    public boolean sendCallEvents = true;
    public boolean sendCallMeta = true;
    public boolean sendClientExtensions = true;
    public boolean sendInboundAudioTracks = true;
    public boolean sendInboundVideoTracks = true;
    public boolean sendOutboundAudioTracks = true;
    public boolean sendOutboundVideoTracks = true;
    public boolean sendClientTransports = true;
    public boolean sendClientDataChannels = true;

    public boolean sendSfuEvents = true;
    public boolean sendSfuMeta = true;
    public boolean sendSfuTransports = true;
    public boolean sendSfuSctpStreams = true;

    public boolean sendSfuInboundRtpStreams = true;
    public boolean sendSfuOutboundRtpStreams = true;

    public boolean sendSfuExtensions = true;
}
