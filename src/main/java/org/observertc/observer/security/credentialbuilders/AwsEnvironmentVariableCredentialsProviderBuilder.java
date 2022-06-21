package org.observertc.observer.security.credentialbuilders;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;

/**
 * Apart from the terrible name this class fetches the configuration for specific type of credentials for AWS,
 * and build the credential provider, so any service we implement for AWS SDK can use this
 */
@Prototype
public class AwsEnvironmentVariableCredentialsProviderBuilder extends AbstractBuilder implements Builder {

    private static final Logger logger = LoggerFactory.getLogger(AwsEnvironmentVariableCredentialsProviderBuilder.class);
    private volatile boolean built = false;

    @Override
    public AwsCredentialsProvider build() {
        if (this.built) {
            throw new IllegalStateException("Cannot build twice");
        }

        var result = EnvironmentVariableCredentialsProvider.create();
        try {
            return result;
        } finally {
            logger.info("{} is built. Configurations: (%s)",
                EnvironmentVariableCredentialsProvider.class.getSimpleName(),
                result.toString()
            );
            this.built = true;
        }
    }

    public static class Config {
    }
}
