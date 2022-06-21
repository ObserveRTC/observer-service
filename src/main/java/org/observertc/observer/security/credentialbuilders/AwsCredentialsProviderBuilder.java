package org.observertc.observer.security.credentialbuilders;

import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AwsCredentialsProviderBuilder extends AbstractBuilder {
    private static final String BUILDER_CLASS_SUFFIX_NAME = "CredentialsProviderBuilder";
    private static final Logger logger = LoggerFactory.getLogger(AwsCredentialsProviderBuilder.class);

    private final List<String> packages;

    public AwsCredentialsProviderBuilder() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());;
    }

    public AwsCredentialsProvider build() {
        Config config = this.convertAndValidate(Config.class);
        String builderClassName = AbstractBuilder.getBuilderClassName("", config.type, BUILDER_CLASS_SUFFIX_NAME);
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.error("Cannot find aws credential builder for {} in packages: {}", config.type, String.join(",", this.packages ));
            return null;
        }
        Builder<AwsCredentialsProvider> awsCredentialsProviderBuilder = (Builder<AwsCredentialsProvider>) builderHolder.get();
        awsCredentialsProviderBuilder.withConfiguration(config.config);
        return awsCredentialsProviderBuilder.build();
    }

    public static class Config {
        public String type;
        public Map<String, Object> config;
    }
}
