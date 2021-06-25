package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.samples.ClientSample;
import org.observertc.webrtc.observer.samples.ClientSampleVisitor;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.security.ObfuscationMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Prototype
public class Obfuscator implements Function<List<ObservedClientSample>, List<ObservedClientSample>> {
    private static final Logger logger = LoggerFactory.getLogger(Obfuscator.class);

    private boolean enabled = false; // change if config enables it
    private java.util.function.Function<String, String> obfuscateUserId;
    private java.util.function.Function<String, String> obfuscateRoomId;
    private java.util.function.Function<String, String> obfuscateIceAddresses;

    public Obfuscator(ObserverConfig observerConfig, ObfuscationMethods obfuscationMethods) {
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
    public List<ObservedClientSample> apply(List<ObservedClientSample> observedClientSampleList) throws Exception{
        if (!this.enabled) {
            return observedClientSampleList;
        }
        for (ObservedClientSample observedClientSample : observedClientSampleList) {
            ClientSample clientSample = observedClientSample.getClientSample();
            clientSample.userId = this.obfuscateUserId.apply(clientSample.userId);
            clientSample.roomId = this.obfuscateRoomId.apply(clientSample.roomId);
            ClientSampleVisitor
                    .streamPeerConnectionTransports(clientSample)
                    .forEach(pcTransport -> {
                        try {
                            pcTransport.localAddress = this.obfuscateIceAddresses.apply(pcTransport.localAddress);
                            pcTransport.remoteAddress = this.obfuscateIceAddresses.apply(pcTransport.remoteAddress);
                        } catch (Throwable t) {
                            logger.error("Error occurred by obfuscating ice addresses", t);
                        }
                    });
        }
        return observedClientSampleList;
    }
}
