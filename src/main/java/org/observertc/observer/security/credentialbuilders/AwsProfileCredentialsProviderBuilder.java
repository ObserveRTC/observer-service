package org.observertc.observer.security.credentialbuilders;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configs.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.profiles.ProfileFile;

import java.nio.file.Path;

/**
 * Apart from the terrible name this class fetches the configuration for specific type of credentials for AWS,
 * and build the credential provider, so any service we implement for AWS SDK can use this
 */
@Prototype
public class AwsProfileCredentialsProviderBuilder extends AbstractBuilder implements Builder {

    private static final Logger logger = LoggerFactory.getLogger(AwsProfileCredentialsProviderBuilder.class);
    private volatile boolean built = false;

    @Override
    public AwsCredentialsProvider build() {
        if (this.built) {
            throw new IllegalStateException("Cannot build twice");
        }
        var config = this.convertAndValidate(Config.class);
        if (config.profileFilePath == null && config.profileName == null) {
            throw new InvalidConfigurationException("profileFile or profileId, must be given");
        }
        var builder = ProfileCredentialsProvider.builder();
        if (config.profileName != null) {
            builder.profileName(config.profileName);
        }
        String profileFileType = null;
        if (config.profileFilePath != null) {
            profileFileType = Utils.firstNotNull(config.profileFileType, ProfileFile.Type.CREDENTIALS.name());
            var path = Path.of(config.profileFilePath);
            var type = ProfileFile.Type.valueOf(profileFileType);
            var profileFile = ProfileFile.builder().content(path).type(type).build();
            builder.profileFile(profileFile);
        }

        try {
            return builder.build();
        } finally {
            logger.info("{} is built. Configurations: (filePath: %s, fileType: %s, profileName: %s)",
                ProfileCredentialsProvider.class.getSimpleName(),
                config.profileFilePath,
                profileFileType,
                config.profileName
            );
            this.built = true;
        }
    }

    public static class Config {
        public String profileFilePath;
        public String profileFileType;
        public String profileName;
    }
}
