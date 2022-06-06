package org.observertc.observer.sinks.firehose;


import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configs.InvalidConfigurationException;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.sinks.Sink;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Prototype
public class FirehoseSinkBuilder extends AbstractBuilder implements Builder<Sink> {

    @Override
    public Sink build() {
        var config = this.convertAndValidate(Config.class);
        AtomicReference<FirehoseClient> clientHolder = new AtomicReference<>(null);
        Supplier<FirehoseClient> clientProvider = () -> {
            var client = clientHolder.get();
            var region = getRegion(config.regionId);
            var credentialsProvider = getCredentialProvider(config);
            return FirehoseClient.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .build();
        };
        var result = new FirehoseSink();
        result.streamName = config.streamName;
        result.clientSupplier = clientProvider;
        result.jsonMapper = JsonMapper.createObjectToBytesMapper();
        return result;
    }

    private static ProfileCredentialsProvider getCredentialProvider(Config config) {
        if (config.profileFilePath == null && config.profileName == null) {
            throw new InvalidConfigurationException("profileFile or profileId, must be given");
        }
        var builder = ProfileCredentialsProvider.builder();
        if (config.profileName != null) {
            builder.profileName(config.profileName);
        }
        if (config.profileFilePath != null) {
            var path = Path.of(config.profileFilePath);
            var type = ProfileFile.Type.valueOf(config.profileFileType);
            var profileFile = ProfileFile.builder().content(path).type(type).build();
            builder.profileFile(profileFile);
        }
        return builder.build();

    }

    private static Region getRegion(String configuredRegion) {
        var foundRegion = Region.regions().stream().filter(awsRegion -> awsRegion.id() == configuredRegion).findFirst();
        if (foundRegion.isEmpty()) {
            String availableRegions = Region.regions().stream().map(region -> region.id()).collect(Collectors.joining(", "));
            throw new InvalidConfigurationException("Invalid AWS region: " + configuredRegion + ": " + availableRegions);
        }
        return foundRegion.get();
    }

    public static class Config {

        @NotNull
        public String regionId;

        @NotNull
        public String streamName;

        public String profileFilePath;
        public String profileFileType;

        public String profileName;

    }
}
