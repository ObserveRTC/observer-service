package org.observertc.webrtc.observer.samples;

import java.util.*;
import java.util.stream.Stream;

/**
 * Call assigned and organized ObservedSamples
 */
public class CollectedClientSamples implements Iterable<ClientSamples> {

    private final Map<UUID, ClientSamples> samples = new HashMap<>();
    private final Map<ServiceRoomId, Set<UUID>> serviceRoomClients = new HashMap<>();
    public static Builder builder() {
        return new Builder();
    }

    public CollectedClientSamples() {

    }



    void add(ClientSamples clientSamples) {
        var clientId = clientSamples.getClientId();
        ClientSamples addedClientSamples = this.samples.get(clientId);
        if (Objects.nonNull(addedClientSamples)) {
            addedClientSamples.addAll(clientSamples);
            return;
        }
        this.samples.put(clientSamples.getClientId(), clientSamples);

        var serviceRoomId = clientSamples.getServiceRoomId();
        Set<UUID> clientIds = this.serviceRoomClients.get(serviceRoomId);
        if (Objects.isNull(clientIds)) {
            clientIds = new HashSet<>();
            this.serviceRoomClients.put(serviceRoomId, clientIds);
        }
        clientIds.add(clientId);

        Set<MediaTrackId> mediaTrackIds = clientSamples.getInboundMediaTrackIds();
        return;
    }

    public Long getMinTimestampOf(ServiceRoomId serviceRoomId) {
        Set<UUID> clientIds = this.serviceRoomClients.get(serviceRoomId);
        if (Objects.isNull(clientIds)) {
            return null;
        }
        Optional<Long> result = clientIds.stream()
                .map(this.samples::get)
                .filter(Objects::nonNull)
                .map(ClientSamples::getMinTimestamp)
                .min(Comparator.naturalOrder());
        if (result.isEmpty()) {
            return null;
        }
        return result.get();
    }

    @Override
    public Iterator<ClientSamples> iterator() {
        return this.samples.values().iterator();
    }

    public Stream<ClientSamples> stream() {
        return this.samples.values().stream();
    }

    public Set<ServiceRoomId> getServiceRoomIds() {
        return this.serviceRoomClients.keySet();
    }

    public Stream<ClientSamples> streamByServiceRoomId(ServiceRoomId serviceRoomId) {
        Set<UUID> clientIds = this.serviceRoomClients.get(serviceRoomId);
        if (Objects.isNull(clientIds)) {
            return Stream.empty();
        }
        return clientIds.stream()
                .map(clientId -> this.samples.get(clientId))
                .filter(Objects::nonNull);
    }

    public static class Builder {
        private final CollectedClientSamples result = new CollectedClientSamples();

        public Builder withClientSamples(ClientSamples clientSamples) {
            this.result.add(clientSamples);
            return this;
        }

        public CollectedClientSamples build() {
            return this.result;
        }
    }

}
