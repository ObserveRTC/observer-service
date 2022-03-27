package org.observertc.observer.components;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.samples.ClientSampleVisitor;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.observer.security.ObfuscationMethods;
import org.observertc.schemas.samples.Samples.ClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Prototype
public class ObservedClientSampleObfuscator implements Function<List<ObservedClientSample>, List<ObservedClientSample>> {
    private static final Logger logger = LoggerFactory.getLogger(ObservedClientSampleObfuscator.class);

    private boolean enabled = false; // change if config enables it
    private java.util.function.Function<String, String> obfuscateUserId;
    private java.util.function.Function<String, String> obfuscateRoomId;
    private java.util.function.Function<String, String> obfuscateIceAddresses;

    public ObservedClientSampleObfuscator(ObserverConfig observerConfig, ObfuscationMethods obfuscationMethods) {
        var config = observerConfig.obfuscations;
        if (Objects.nonNull(config)) {
            this.enabled = config.enabled;
            this.obfuscateUserId = obfuscationMethods.makeMethodForString(config.maskedUserId);
            this.obfuscateRoomId = obfuscationMethods.makeMethodForString(config.maskedRoomId);
            this.obfuscateIceAddresses = obfuscationMethods.makeMethodForString(config.maskedIceAddresses);
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
