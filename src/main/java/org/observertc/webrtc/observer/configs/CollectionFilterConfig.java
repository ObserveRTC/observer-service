package org.observertc.webrtc.observer.configs;

import org.observertc.webrtc.observer.common.ObjectToString;

import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.Objects;

public class CollectionFilterConfig {

    public static Builder builder() { return new Builder();}

    @Min(-1)
    public int gt = -1;

    @Min(-1)
    public int eq = -1;

    @Min(-1)
    public int lt = -1;


    public String[] anyMatch = new String[0];
    public String[] allMatch = new String[0];

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
            return false;
        }
        CollectionFilterConfig otherDTO = (CollectionFilterConfig) other;
        if (this.gt != otherDTO.gt) return false;
        if (this.eq != otherDTO.eq) return false;
        if (this.lt != otherDTO.lt) return false;
        if (!Arrays.equals(this.allMatch, otherDTO.allMatch)) return false;
        if (!Arrays.equals(this.anyMatch, otherDTO.anyMatch)) return false;
        return true;
    }


    public static class Builder {
        private CollectionFilterConfig result = new CollectionFilterConfig();

        public Builder numOfElementsIsGreaterThan(int value) {
            this.result.gt = value;
            return this;
        }

        public Builder numOfElementsIsLessThan(int value) {
            this.result.lt = value;
            return this;
        }

        public Builder numOfElementsIsEqualTo(int value) {
            this.result.eq = value;
            return this;
        }

        public Builder anyOfTheElementsAreMatchingTo(String... elements) {
            Objects.requireNonNull(elements);
            this.result.anyMatch = elements;
            return this;
        }

        public Builder allOfTheElementsAreMatchingTo(String... elements) {
            Objects.requireNonNull(elements);
            this.result.allMatch = elements;
            return this;
        }

        public CollectionFilterConfig build() {
            return this.result;
        }
    }
}
