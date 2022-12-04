package org.observertc.observer.hamokdiscovery;

public record HamokConnectionStateChangedEvent(
        HamokConnection hamokConnection,
        HamokConnectionState state
) {
    @Override
    public String toString() {
        return String.format("hamokConnection: %s, state: %s", hamokConnection, state);
    }
}
