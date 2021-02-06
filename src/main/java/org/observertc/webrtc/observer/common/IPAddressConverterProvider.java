package org.observertc.webrtc.observer.common;

import org.observertc.webrtc.observer.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.Function;

@Singleton
public class IPAddressConverterProvider {

    private static final Logger logger = LoggerFactory.getLogger(IPAddressConverterProvider.class);

    @Inject
    ObserverConfig.IPAddressConverterConfig config;

    @PostConstruct
    void setup() {

    }

    public Function<String, String> provide() {
        if (!this.config.enabled) {
            return Function.identity();
        }
        try {
            return this.makeHashDigester();
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Error occurred by creating converter", e);
            return Function.identity();
        }
    }

    private Function<String, String> makeHashDigester() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(this.config.algorithm);
        byte[] salt = this.config.salt.getBytes(StandardCharsets.UTF_8);
        return new Function<String, String>() {
            @Override
            public String apply(String s) {
                if (Objects.isNull(s)) {
                    return null;
                }
                digest.reset();
                digest.update(s.getBytes(StandardCharsets.UTF_8));
                digest.update(salt);
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
