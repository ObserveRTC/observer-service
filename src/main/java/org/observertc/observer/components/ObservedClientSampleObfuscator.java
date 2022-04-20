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
    private java.util.function.Function<String, String> userIdObfuscator;
    private java.util.function.Function<String, String> roomIdObfuscator;
    private java.util.function.Function<String, String> iceAddressObfuscator;

    public ObservedClientSampleObfuscator(ObserverConfig observerConfig, ObfuscationMethods obfuscationMethods) {
        var config = observerConfig.evaluators.obfuscator;
        if (Objects.nonNull(config)) {
            this.enabled = config.enabled;
            this.userIdObfuscator = obfuscationMethods.builder(config.userId).buildForString();
            this.roomIdObfuscator = obfuscationMethods.builder(config.roomId).buildForString();
            this.iceAddressObfuscator = obfuscationMethods.builder(config.iceAddresses).buildForString();
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
            clientSample.userId = this.userIdObfuscator.apply(clientSample.userId);
            clientSample.roomId = this.roomIdObfuscator.apply(clientSample.roomId);
            ClientSampleVisitor
                    .streamPeerConnectionTransports(clientSample)
                    .forEach(pcTransport -> {
                        try {
                            pcTransport.localAddress = this.iceAddressObfuscator.apply(pcTransport.localAddress);
                            pcTransport.remoteAddress = this.iceAddressObfuscator.apply(pcTransport.remoteAddress);
                        } catch (Throwable t) {
                            logger.error("Error occurred by obfuscating ice addresses", t);
                        }
                    });
        }
        return observedClientSampleList;
    }
}
