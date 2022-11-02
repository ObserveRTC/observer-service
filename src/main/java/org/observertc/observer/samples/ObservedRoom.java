package org.observertc.observer.samples;

import org.jetbrains.annotations.NotNull;
import org.observertc.observer.common.FlatIterator;
import org.observertc.schemas.samples.Samples;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public interface ObservedRoom extends Iterable<ObservedClient> {

    ServiceRoomId getServiceRoomId();
    String getCallId();
    String getMarker();
    Long getMinTimestamp();
    Long getMaxTimestamp();

    Iterable<ObservedClientSample> observedClientSamples();
    void setCallId(String callId);


    class Builder implements ObservedRoom {
        final ObservedClientSamples.Builder observedClientSamples;
        private final ServiceRoomId serviceRoomId;
        private String callId = null;

        private Map<String, ObservedClient> clients = new HashMap<>();

        Builder(ObservedClientSamples.Builder observedClientSamples, ServiceRoomId serviceRoomId) {
            this.observedClientSamples = observedClientSamples;
            this.serviceRoomId = serviceRoomId;
        }

        public void add(String mediaUnitId, Samples.ClientSample clientSample) {
            if (clientSample.clientId == null) {
                return;
            }
            ObservedClient.Builder observedClient = (ObservedClient.Builder) this.clients.get(clientSample.clientId);
            if (observedClient == null) {
                observedClient = new ObservedClient.Builder(this, mediaUnitId, clientSample.clientId);
                this.clients.put(observedClient.getClientId(), observedClient);
            }
            observedClient.add(clientSample);
            if (this.callId == null) {
                if (clientSample.callId != null) {
                    this.observedClientSamples.callIds.add(clientSample.callId);
                }
                this.callId = clientSample.callId;
            }
        }

        @Override
        public void setCallId(String callId) {
            if (this.callId != null) {
                this.observedClientSamples.callIds.remove(this.callId);
            }

            for (var observedClient : this.clients.values()) {
                for (var observedClientSample : observedClient.observedClientSamples()) {
                    var clientSample = observedClientSample.getClientSample();
                    if (clientSample.callId != null) {
                        this.observedClientSamples.callIds.remove(clientSample.callId);
                    }
                    clientSample.callId = callId;
                }
            }
            this.callId = callId;
            this.observedClientSamples.callIds.add(this.callId);
        }

        @Override
        public ServiceRoomId getServiceRoomId() {
            return this.serviceRoomId;
        }

        @Override
        public String getCallId() {
            return this.callId;
        }

        @Override
        public String getMarker() {
            return this.clients.values().stream()
                    .map(ObservedClient::getClientId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Long getMinTimestamp() {
            return this.clients.values().stream()
                    .map(ObservedClient::getMinTimestamp)
                    .min(Long::compare)
                    .orElse(null);
        }

        @Override
        public Long getMaxTimestamp() {
            return this.clients.values().stream()
                    .map(ObservedClient::getMaxTimestamp)
                    .max(Long::compare)
                    .orElse(null);
        }

        @NotNull
        @Override
        public Iterator<ObservedClient> iterator() {
            return this.clients.values().iterator();
        }

        @Override
        public Iterable<ObservedClientSample> observedClientSamples() {

            return () -> new FlatIterator<ObservedClientSample>(
                    clients.values().iterator(),
                    client -> client.observedClientSamples().iterator()
            );
        }
    }
}
