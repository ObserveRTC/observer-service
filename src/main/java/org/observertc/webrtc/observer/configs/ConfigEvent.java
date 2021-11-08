package org.observertc.webrtc.observer.configs;

import java.util.Objects;

public class ConfigEvent<T> {

    public static Builder<ObserverReportConfig> makeObserverReportConfigBuilder() {
        return new Builder<>().withConfigType(ConfigType.OBSERVER_REPORT);
    }

    private ConfigOperation operation;
    private ConfigType type;
    private T config;


    ConfigEvent() {
    }

    public ConfigOperation getOperationType() {
        return this.operation;
    }

    public T getConfig() {
        return this.config;
    }

    public ConfigType getType() {
        return this.type;
    }

    public static class Builder<R> {
        private final ConfigEvent result = new ConfigEvent();
        private Builder() {

        }

        public Builder withConfigType(ConfigType value) {
            this.result.type = value;
            return this;
        }

        public Builder withConfig(R value) {
            this.result.config = value;
            return this;
        }

        public Builder withOperationType(ConfigOperation value) {
            this.result.operation = value;
            return this;
        }

        public ConfigEvent<R> build() {
            Objects.requireNonNull(this.result.type);
            Objects.requireNonNull(this.result.config);
            Objects.requireNonNull(this.result.operation);
            return this.result;
        }
    }

}
