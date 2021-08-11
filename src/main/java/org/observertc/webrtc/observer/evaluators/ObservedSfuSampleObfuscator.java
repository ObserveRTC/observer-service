package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.samples.ObservedSfuSample;
import org.observertc.webrtc.observer.samples.SfuSample;
import org.observertc.webrtc.observer.samples.SfuSampleVisitor;
import org.observertc.webrtc.observer.security.ObfuscationMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Prototype
public class ObservedSfuSampleObfuscator implements Function<List<ObservedSfuSample>, List<ObservedSfuSample>> {
    private static final Logger logger = LoggerFactory.getLogger(ObservedSfuSampleObfuscator.class);

    private boolean enabled = false; // change if config enables it
    private java.util.function.Function<String, String> obfuscateUserId;
    private java.util.function.Function<String, String> obfuscateRoomId;
    private java.util.function.Function<String, String> obfuscateIceAddresses;

    public ObservedSfuSampleObfuscator(ObserverConfig observerConfig, ObfuscationMethods obfuscationMethods) {
        var config = observerConfig.obfuscations;
        if (Objects.nonNull(config)) {
            this.enabled = config.enabled;
            this.obfuscateUserId = obfuscationMethods.makeMethodForString(config.obfuscateUserId);
            this.obfuscateRoomId = obfuscationMethods.makeMethodForString(config.obfuscateRoomId);
            this.obfuscateIceAddresses = obfuscationMethods.makeMethodForString(config.obfuscateIceAddresses);
        }
    }


    @PostConstruct
    void setup() {

    }

    @Override
    public List<ObservedSfuSample> apply(List<ObservedSfuSample> ObservedSfuSampleList) throws Exception{
        if (!this.enabled) {
            return ObservedSfuSampleList;
        }
        for (ObservedSfuSample observedSfuSample : ObservedSfuSampleList) {
            SfuSample sfuSample = observedSfuSample.getSfuSample();
            SfuSampleVisitor
                    .streamTransports(sfuSample)
                    .forEach(sfuTransport -> {
                        try {
                            sfuTransport.localAddress = this.obfuscateIceAddresses.apply(sfuTransport.localAddress);
                            sfuTransport.remoteAddress = this.obfuscateIceAddresses.apply(sfuTransport.remoteAddress);
                        } catch (Throwable t) {
                            logger.error("Error occurred by obfuscating ice addresses", t);
                        }
                    });
        }
        return ObservedSfuSampleList;
    }
}
