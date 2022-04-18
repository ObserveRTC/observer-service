package org.observertc.observer.sources;

import org.observertc.observer.configs.TransportFormatType;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

interface Acceptor {
    String getMediaUnitId();

    String getServiceId();

    void accept(byte[] message);

    Acceptor onError(Consumer<Throwable> listener);

    static Acceptor create(Logger logger, String mediaUnitId, String serviceId, String schemaVersion, TransportFormatType format, Consumer<ReceivedSamples> forward) {
        Objects.requireNonNull(forward, "Forward consumer must be provided to build an Acceptor");
        Objects.requireNonNull(format, "Format must be provided");
        SamplesDecoder decoder = SamplesDecoder.builder(logger)
                .withFormatType(format)
                .withVersion(schemaVersion)
                .build();
        var errorListeners = new LinkedList<Consumer<Throwable>>();
        var result = new Acceptor() {
            @Override
            public String getMediaUnitId() {
                return mediaUnitId;
            }

            @Override
            public String getServiceId() {
                return serviceId;
            }

            @Override
            public Acceptor onError(Consumer<Throwable> listener) {
                errorListeners.add(listener);
                return this;
            }

            @Override
            public void accept(byte[] message) {
                try {
                    var samples = decoder.decode(message);
                    var receivedSamples = ReceivedSamples.of(
                            serviceId,
                            mediaUnitId,
                            samples
                    );
                    forward.accept(receivedSamples);
                } catch(Throwable ex) {
                    if (errorListeners.size() < 1) throw new RuntimeException(ex.getMessage());
                    errorListeners.stream().forEach(listener -> listener.accept(ex));
                }
            }
        };
        return result;
    }
}
