package org.observertc.observer.security.credentialbuilders;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;

import java.nio.file.Path;

/**
 * Apart from the terrible name this class fetches the configuration for specific type of credentials for AWS,
 * and build the credential provider, so any service we implement for AWS SDK can use this
 */
@Prototype
public class AwsWebIdentityTokenFileCredentialsProviderBuilder extends AbstractBuilder implements Builder<AwsCredentialsProvider> {

    private static final Logger logger = LoggerFactory.getLogger(AwsWebIdentityTokenFileCredentialsProviderBuilder.class);
    private volatile boolean built = false;

    @Override
    public AwsCredentialsProvider build() {
        if (this.built) {
            throw new IllegalStateException("Cannot build twice");
        }

        var config = this.convertAndValidate(Config.class);
        var builder = WebIdentityTokenFileCredentialsProvider.builder();
        if (config.roleArn != null) {
            builder.roleArn(config.roleArn);
        }
        if (config.roleSessionName != null) {
            builder.roleSessionName(config.roleSessionName);
        }
        if (config.tokenFile != null) {
            var path = Path.of(config.tokenFile);
            builder.webIdentityTokenFile(path);
        }

        try {
            return builder.build();
        } finally {
            logger.info("{} is built. Configurations: (%s)",
                WebIdentityTokenFileCredentialsProvider.class.getSimpleName(),
                JsonUtils.objectToString(config)
            );
            this.built = true;
        }
    }

    public static class Config {
        public String roleArn;
        public String roleSessionName;
        public String tokenFile;
    }
}
