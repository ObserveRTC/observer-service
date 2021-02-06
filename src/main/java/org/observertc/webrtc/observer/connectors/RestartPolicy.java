package org.observertc.webrtc.observer.connectors;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public enum RestartPolicy {
    Never,
    OnFailure,
    Always
    ;

    public static Optional<RestartPolicy> getValueFromString(@NotNull String value, AtomicReference<String> errorMessage) {
        RestartPolicy[] policies = RestartPolicy.values();
        for (int i = 0; i < policies.length; ++i) {
            RestartPolicy policy = policies[i];
            try {
                if (policy.name().toLowerCase().equals(value.toLowerCase())) {
                    return Optional.of(policy);
                }
            } catch (Exception ex) {
                if (Objects.nonNull(errorMessage)) {
                    errorMessage.set("For value string (" + value + ") an error happened during conversion to RestartPolicy: " +  ex.getMessage());
                }
                return Optional.empty();
            }
        }
        if (Objects.nonNull(errorMessage)) {
            errorMessage.set("No match for value "+value + " in RestartPolicy. possible values are:" + String.join(", ", Arrays.stream(policies).map(Enum::name).collect(Collectors.toList())));
        }
        return Optional.empty();
    }
}
