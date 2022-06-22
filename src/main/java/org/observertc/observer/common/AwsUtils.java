package org.observertc.observer.common;

import org.observertc.observer.configs.InvalidConfigurationException;
import software.amazon.awssdk.regions.Region;

import java.util.stream.Collectors;

public final class AwsUtils {
    public static Region getRegion(String configuredRegion) {
        var foundRegion = Region.regions().stream().filter(awsRegion -> awsRegion.id().equals(configuredRegion)).findFirst();
        if (foundRegion.isEmpty()) {
            String availableRegions = Region.regions().stream().map(region -> region.id()).collect(Collectors.joining(", "));
            throw new InvalidConfigurationException("Invalid AWS region: " + configuredRegion + ": " + availableRegions);
        }
        return foundRegion.get();
    }
}
