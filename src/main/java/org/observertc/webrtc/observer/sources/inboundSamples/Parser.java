package org.observertc.webrtc.observer.sources.inboundSamples;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.samples.Samples;

public interface Parser extends Function<byte[], Samples> {
}
