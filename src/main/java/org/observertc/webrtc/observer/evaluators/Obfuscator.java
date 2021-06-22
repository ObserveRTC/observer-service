package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.configs.ObfuscationsConfig;
import org.observertc.webrtc.observer.samples.ClientSample;
import org.observertc.webrtc.observer.samples.ClientSampleVisitor;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Prototype
public class Obfuscator implements Function<List<ObservedClientSample>, List<ObservedClientSample>> {
    private static final Logger logger = LoggerFactory.getLogger(Obfuscator.class);

    private volatile boolean enabled = false; // change if config enables it

    private AtomicReference<ObfuscationsConfig> config = new AtomicReference<>();
    private Function<String, String> digest;

    @PostConstruct
    void setup() {
        // TODO: catch new configs, and apply
    }

    private void update(ObfuscationsConfig newConfig) {
        boolean disable = false;
        try {
            if (!newConfig.enabled) {
                disable = true;
                return;
            }
            this.digest = makeHashDigester(newConfig.hashAlgorithm, newConfig.salt);
            this.config.set(newConfig);
            this.enabled = true;
        } catch (Throwable t) {
            logger.error("An error occurred by applying the Obfuscation Configuration.", t);
            disable = true;
        } finally {
            if (disable) {
                this.enabled = false;
                this.config.set(null);
                this.digest = null;
                logger.info("Obfuscation is disabled");
            }
        }
    }

    @Override
    public List<ObservedClientSample> apply(List<ObservedClientSample> observedClientSampleList) throws Exception{
        if (!this.enabled) {
            return observedClientSampleList;
        }
        ObfuscationsConfig config = this.config.get();
        if (Objects.isNull(config)) {
            logger.warn("Obfuscator is enabled, but no config was available. Obfuscation will not work");
            return observedClientSampleList;
        }
        if (Objects.isNull(this.digest)) {
            logger.warn("Obfuscation is enabled, but there is no digest function created. Please check the configuration of your obfuscation algorithm, or report a bug. No obfuscation is done.");
            return observedClientSampleList;
        }
        try {
            this.obfuscate(config, observedClientSampleList);
        } catch (Throwable throwable) {
            logger.warn("An error occurred during obfuscation. ", throwable);
        }
        return observedClientSampleList;
    }

    private void obfuscate(ObfuscationsConfig config, List<ObservedClientSample> observedClientSampleList) throws Throwable {
        for (ObservedClientSample observedClientSample : observedClientSampleList) {
            ClientSample clientSample = observedClientSample.getClientSample();
            if (config.obfuscateUserId) {
                clientSample.userId = this.digest.apply(clientSample.clientId);
            }
            if (config.obfuscateRoomId) {
                clientSample.roomId = this.digest.apply(clientSample.roomId);
            }
            if (config.obfuscateIceAddresses) {
                ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                        .forEach(pcTransport -> {
                            try {
                                pcTransport.localAddress = this.digest.apply(pcTransport.localAddress);
                                pcTransport.remoteAddress = this.digest.apply(pcTransport.remoteAddress);
                            } catch (Throwable t) {
                                logger.error("Error occurred by obfuscating ice addresses", t);
                            }
                        });
            }
        }
    }

    private static Function<String, String> makeHashDigester(String algorithm, String salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        return new Function<String, String>() {
            @Override
            public String apply(String inputString) {
                if (Objects.isNull(inputString)) {
                    return null;
                }
                byte[] input = inputString.getBytes(StandardCharsets.UTF_8);
                digest.reset();
                digest.update(input);
                digest.update(saltBytes);
                byte[] bytes = digest.digest();
                StringBuilder hexString = new StringBuilder(2 * bytes.length);
                for (int i = 0; i < bytes.length; i++) {
                    hexString.append(Integer.toString((int)bytes[i]));
                    hexString.append(".");
                }
                return hexString.toString();
            }
        };
    }
}
