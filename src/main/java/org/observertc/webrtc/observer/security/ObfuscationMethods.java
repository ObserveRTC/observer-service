package org.observertc.webrtc.observer.security;

import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Singleton
public class ObfuscationMethods {

    private static final Logger logger = LoggerFactory.getLogger(ObfuscationMethods.class);


    private Supplier<Function<byte[], byte[]>> hashSupplier;

    public ObfuscationMethods(ObserverConfig observerConfig) {
        var config = observerConfig.obfuscations;
        if (Objects.nonNull(observerConfig.obfuscations) && Objects.nonNull(observerConfig.obfuscations.anonymization)) {
            this.hashSupplier = makeHashDigesterSupplier(observerConfig.obfuscations.anonymization);
        } else {
            this.hashSupplier = () -> Function.identity();
        }

    }

    @PostConstruct
    void setup() {

    }

    public Function<String, String> makeMethodForString(ObserverConfig.ObfuscationsConfig.ObfuscationType obfuscationType) {
        return this.makeMethodForString(obfuscationType, StandardCharsets.UTF_8);
    }

    public Function<String, String> makeMethodForString(ObserverConfig.ObfuscationsConfig.ObfuscationType obfuscationType, Charset charset) {
        Function<byte[], byte[]> obfuscateBytes = this.makeMethodForBytes(obfuscationType);
        return input -> {
            if (Objects.isNull(input)) {
                return null;
            }
            var bytesInput = input.getBytes(charset);
            var bytesOutput = obfuscateBytes.apply(bytesInput);
            return new String(bytesOutput, charset);
        };
    }

    public Function<byte[], byte[]> makeMethodForBytes(ObserverConfig.ObfuscationsConfig.ObfuscationType obfuscationType) {
        if (Objects.isNull(obfuscationType)) {
            logger.warn("No obfuscation type has been given");
            return Function.identity();
        }
        Function<byte[], byte[]> noneSupplier = Function.identity();
        var resultProvider = ObfuscationTypeVisitor.<Function<byte[], byte[]>>makeSupplierVisitor(
                this.hashSupplier,
                () -> Function.identity()
        );
        return resultProvider.apply(null, obfuscationType);
    }

    private static Supplier<Function<byte[], byte[]>> makeHashDigesterSupplier(ObserverConfig.ObfuscationsConfig.ObfuscationsAnonymizationConfig config) {
        try {
            Objects.requireNonNull(config.salt);
            Objects.requireNonNull(config.hashAlgorithm);
            byte[] salt = config.salt.getBytes(StandardCharsets.UTF_8);
            return () -> {
                try {
                    return makeHashDigester(salt, config.hashAlgorithm);
                } catch (Exception ex) {
                    logger.error("Cannot make hash algorithm for obfuscator. config: {}", ObjectToString.toString(config), ex);
                    return Function.identity();
                }
            };
        } catch (Throwable t) {
            logger.error("Cannot make hash algorithm for obfuscator. config: {}", ObjectToString.toString(config), t);
            return () -> Function.identity();
        }
    }

    private static Function<byte[], byte[]> makeHashDigester(byte[] salt, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return new Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] input) {
                if (Objects.isNull(input)) {
                    return null;
                }
                digest.reset();
                digest.update(input);
                digest.update(salt);
                byte[] result = digest.digest();
                return result;
            }
        };
    }
}
