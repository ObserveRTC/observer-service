package org.observertc.observer.hamokdiscovery;

public record HamokConnectionStateChangedEvent(
        HamokConnection hamokConnection,
        HamokConnectionState prevState,
        HamokConnectionState actualState
) {
    @Override
    public String toString() {
        return String.format("hamokConnection: %s, prevState: %s, actualState: {}", hamokConnection, prevState, actualState);
    }
}
