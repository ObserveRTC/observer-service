package org.observertc.webrtc.observer.common;

import java.util.Objects;

public class KeyValuePair<K, V> {
    public static<U, R> Builder<U, R> builder() {
        return new Builder();
    }

    public static<U, R> KeyValuePair<U, R> of(U key, R value) {
        return builder()
                .withKey(key)
                .withValue(value)
                .build();
    }

    private K key = null;
    private V value = null;

    private KeyValuePair() {

    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.getValue();
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || other instanceof KeyValuePair == false) {
            return false;
        }
        KeyValuePair<K, V> peer = (KeyValuePair<K, V>) other;
        return this.key.equals(peer.key) && this.value.equals(peer.value);
    }

    private static class Builder<U, R> {
        private KeyValuePair<U, R> result = new KeyValuePair<>();

        public Builder withKey(U key) {
            this.result.key = key;
            return this;
        }

        public Builder withValue(R value) {
            this.result.value = value;
            return this;
        }

        public KeyValuePair<U, R> build() {
            Objects.requireNonNull(this.result.key);
            Objects.requireNonNull(this.result.value);
            return this.result;
        }
    }
}
