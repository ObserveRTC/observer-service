package org.observertc.webrtc.observer.sources.inboundSamples;

public interface InboundSamplesAcceptor {
    void accept(String serviceId, String mediaUnitId, byte[] message) throws Throwable;
}
