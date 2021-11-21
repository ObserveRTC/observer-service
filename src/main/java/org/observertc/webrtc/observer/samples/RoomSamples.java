package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.common.UUIDAdapter;

import java.util.*;
import java.util.stream.Stream;

public class RoomSamples implements Iterable<ClientSamples> {

    public static RoomSamples.Builder builderFrom(ServiceRoomId serviceRoomId) {
        return new Builder().withServiceRoomId(serviceRoomId);
    }

    private ServiceRoomId serviceRoomId;
    final Map<UUID, ClientSamples> clientSamples = new HashMap<>();
    Long minTimestamp = null;
    Long maxTimestamp = null;
    UUID callId = null;

    private RoomSamples() {

    }


    @Override
    public Iterator<ClientSamples> iterator() {
        return this.clientSamples.values().iterator();
    }

    public Stream<ClientSamples> stream() {
        return this.clientSamples.values().stream();
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public Long getMaxTimestamp() {
        return this.maxTimestamp;
    }

    public Long getMinTimestamp() {
        return this.minTimestamp;
    }

    public UUID getCallId() {
        return this.callId;
    }

    public static class Builder {
        private final RoomSamples result = new RoomSamples();
        private final Map<UUID, ClientSamples.Builder> builders = new HashMap<>();

        private Builder() {

        }

        private RoomSamples.Builder withServiceRoomId(ServiceRoomId serviceRoomId) {
            this.result.serviceRoomId = serviceRoomId;
            return this;
        }

        public RoomSamples.Builder withObservedClientSample(ObservedClientSample observedClientSample) {
            var clientId = observedClientSample.getClientId();
            ClientSamples.Builder clientSamplesBuilder = this.builders.get(clientId);
            if (Objects.isNull(clientSamplesBuilder)) {
                clientSamplesBuilder = ClientSamples.builderFrom(observedClientSample);
                this.builders.put(clientId, clientSamplesBuilder);
            }
            var clientSample = observedClientSample.getClientSample();
            clientSamplesBuilder.withClientSample(clientSample);

            if (Objects.isNull(this.result.minTimestamp) || this.result.minTimestamp < clientSample.timestamp) {
                this.result.minTimestamp = clientSample.timestamp;
            }
            if (Objects.isNull(this.result.maxTimestamp) || clientSample.timestamp < this.result.maxTimestamp) {
                this.result.maxTimestamp = clientSample.timestamp;
            }
            if (Objects.nonNull(clientSample.callId) && Objects.isNull(this.result.callId)) {
                this.result.callId = UUIDAdapter.tryParseOrNull(clientSample.callId);
            }
            return this;
        }

        public RoomSamples build() {
            Objects.requireNonNull(this.result.serviceRoomId);
            this.builders.values()
                    .stream()
                    .map(ClientSamples.Builder::build)
                    .forEach(clientSamples -> this.result.clientSamples.put(clientSamples.getClientId(), clientSamples));
            return this.result;
        }
    }
}
