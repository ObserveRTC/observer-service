package org.observertc.observer.security.credentialbuilders;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.AwsUtils;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Apart from the terrible name this class fetches the configuration for specific type of credentials for AWS,
 * and build the credential provider, so any service we implement for AWS SDK can use this
 */
@Prototype
public class AwsStsAssumeRoleSessionCredentialsProviderBuilder extends AbstractBuilder implements Builder<AwsCredentialsProvider> {

    private static final Logger logger = LoggerFactory.getLogger(AwsStsAssumeRoleSessionCredentialsProviderBuilder.class);



    private volatile boolean built = false;

    @Override
    public AwsCredentialsProvider build() {
        if (this.built) {
            throw new IllegalStateException("Cannot build twice");
        }
        var config = this.convertAndValidate(Config.class);
        var assumeRoleRequestBuilder = AssumeRoleRequest.builder()
                .roleSessionName(config.roleSessionName)
                .roleArn(config.roleArn);

        if (config.externalId != null) {
            assumeRoleRequestBuilder.externalId(config.externalId);
        }
        if (config.policy != null) {
            assumeRoleRequestBuilder.policy(config.policy);
        }
        if (config.serialNumber != null) {
            assumeRoleRequestBuilder.serialNumber(config.serialNumber);
        }
        if (config.transitiveTagKey != null) {
            assumeRoleRequestBuilder.transitiveTagKeys(config.transitiveTagKey);
        }
        var assumeRoleRequest = assumeRoleRequestBuilder.build();
        var regionId = AwsUtils.getRegion(config.regionId);
        var stsClientBuilder = StsClient.builder()
                .region(regionId);
        if (config.credentials != null) {
            var stsCredentialsProviderBuilder = new AwsCredentialsProviderBuilder();
            stsCredentialsProviderBuilder.withConfiguration(config.credentials);
            var stsCredentialsProvider = stsCredentialsProviderBuilder.build();
            stsClientBuilder.credentialsProvider(stsCredentialsProvider);
            logger.info("Embedded client provider for sts client. AwsCredentials: {}, config: {}", stsCredentialsProvider.getClass().getSimpleName(), JsonUtils.objectToString(config.credentials));
        }
        var stsClient = stsClientBuilder.build();
        logger.info("STS Client: {}", stsClient);
        try {
            return StsAssumeRoleCredentialsProvider.builder()
                    .stsClient(stsClient)
                    .refreshRequest(assumeRoleRequest)
                    .build();
        } finally {
            logger.info("{} is built. Configurations: (%s)",
                StsAssumeRoleCredentialsProvider.class.getSimpleName(),
                JsonUtils.objectToString(config)
            );
            this.built = true;
        }
    }

    public static class Config {

        @NotNull
        public String regionId;

        public String transitiveTagKey;

        public String serialNumber;

        public String policy;

        public String externalId;

        @NotNull
        public String roleArn;

        public String roleSessionName = "observer";

        public Map<String, Object> credentials;

    }
}
