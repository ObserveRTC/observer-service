package org.observertc.webrtc.observer.common;

import org.observertc.webrtc.observer.dto.GeneralEntryDTO;

import java.util.Objects;
import java.util.UUID;

public class ClientMessageEvent {
    public static Builder builder() {
        return new Builder();
    }

    public static ClientMessageEvent of(UUID clientId, GeneralEntryDTO value) {
        return builder()
                .withClientId(clientId)
                .withValue(value)
                .build();
    }

    private UUID clientId = null;
    private GeneralEntryDTO value = null;

    private ClientMessageEvent() {

    }

    public UUID getClientId() {
        return this.clientId;
    }

    public GeneralEntryDTO getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.clientId.hashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || other instanceof ClientMessageEvent == false) {
            return false;
        }
        ClientMessageEvent peer = (ClientMessageEvent) other;
        return this.clientId.equals(peer.clientId) && this.value.equals(peer.value);
    }

    private static class Builder {
        private ClientMessageEvent result = new ClientMessageEvent();

        public Builder withClientId(UUID clientId) {
            this.result.clientId = clientId;
            return this;
        }

        public Builder withValue(GeneralEntryDTO value) {
            this.result.value = value;
            return this;
        }

        public ClientMessageEvent build() {
            Objects.requireNonNull(this.result.clientId);
            Objects.requireNonNull(this.result.value);
            return this.result;
        }
    }
}
