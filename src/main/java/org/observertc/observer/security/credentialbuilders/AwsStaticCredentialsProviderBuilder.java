package org.observertc.observer.security.credentialbuilders;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import javax.validation.constraints.NotNull;
import java.util.Random;

/**
 * Apart from the terrible name this class fetches the configuration for specific type of credentials for AWS,
 * and build the credential provider, so any service we implement for AWS SDK can use this
 */
@Prototype
class AwsStaticCredentialsProviderBuilder extends AbstractBuilder implements Builder {

    private static final Logger logger = LoggerFactory.getLogger(AwsStaticCredentialsProviderBuilder.class);
    private volatile boolean built = false;

    @Override
    public AwsCredentialsProvider build() {
        if (this.built) {
            throw new IllegalStateException("Cannot built twice");
        }
        var config = this.convertAndValidate(Config.class);
        try {
            return StaticCredentialsProvider.create(
                    new AwsCredentials() {
                        @Override
                        public String accessKeyId() {
                            return config.accessKeyId;
                        }

                        @Override
                        public String secretAccessKey() {
                            return config.secretAccessKey;
                        }
                    }
            );
        } finally {
            logger.info("{} is built. Configurations: (accessKeyId: %s, secretAccessKey: %s)",
                    StaticCredentialsProvider.class.getSimpleName(),
                    config.accessKeyId,
                    "x".repeat(config.secretAccessKey.length() + new Random().nextInt(10))
            );
            this.built = true;
        }
    }

    public static class Config {

        @NotNull
        public String accessKeyId;

        @NotNull
        public String secretAccessKey;
    }
}
