package org.observertc.webrtc.observer.sources;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.dto.pcsamples.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.samples.ClientSample;

import java.util.function.Function;

@Prototype
public class PCSampleToClientSampleConverter implements Function<PeerConnectionSample, ClientSample> {
    @Override
    public ClientSample apply(PeerConnectionSample peerConnectionSample) {
        throw new RuntimeException("Not implemented method");
    }
}
