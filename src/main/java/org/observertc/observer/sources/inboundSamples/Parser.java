package org.observertc.observer.sources.inboundSamples;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.observer.samples.Samples;

public interface Parser extends Function<byte[], Samples> {
}
