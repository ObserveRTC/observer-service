package org.observertc.observer.hamokendpoints.websocket;

public interface ConnectionBuffer {

    static ConnectionBuffer discardingBuffer() {
        return new ConnectionBuffer() {
            @Override
            public void add(byte[] message) {
                return;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public byte[] poll() {
                return null;
            }
        };
    }

    void add(byte[] message);
    boolean isEmpty();
    byte[] poll();
}
