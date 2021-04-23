package org.observertc.webrtc.observer.configs;

import org.observertc.webrtc.observer.common.ObjectToString;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class SentinelConfig {
    public static SentinelConfig.Builder builder() {
        return new SentinelConfig.Builder();
    }

    @NotNull
    public String name;
    public boolean report = false;
    public boolean expose = false;

    public Filters callFilters = new Filters();
    public Filters pcFilters = new Filters();

    public static class Filters {
        public String[] anyMatch = new String[0];
        public String[] allMatch = new String[0];
    }

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }
        SentinelConfig otherDTO = (SentinelConfig) other;
        if (this.name != otherDTO.name) return false;
        if (this.report != otherDTO.report) return false;
        if (this.expose != otherDTO.expose) return false;
        return true;
    }

    public static class Builder {
        private SentinelConfig result = new SentinelConfig();

        public Builder withName(String name) {
            this.result.name = name;
            return this;
        }

        public Builder withReport(boolean value) {
            this.result.report = value;
            return this;
        }

        public Builder withExpose(boolean value) {
            this.result.expose = value;
            return this;
        }

        public Builder withAllCallMatchFilterNames(String... values) {
            this.result.callFilters.allMatch = values;
            return this;
        }

        public Builder withAnyCallMatchFilterNames(String... values) {
            this.result.callFilters.anyMatch = values;
            return this;
        }

        public Builder withAllPCMatchFilterNames(String... values) {
            this.result.pcFilters.allMatch = values;
            return this;
        }

        public Builder withAnyPCMatchFilterNames(String... values) {
            this.result.pcFilters.anyMatch = values;
            return this;
        }

        public SentinelConfig build() {
            return this.result;
        }

    }

}
