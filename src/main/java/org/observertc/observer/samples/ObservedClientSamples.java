package org.observertc.observer.samples;

import java.util.*;
import java.util.stream.Stream;

public interface ObservedClientSamples extends Iterable<ObservedClientSample> {

    static Builder builder() {
        return new Builder();
    }

    boolean isEmpty();

    Stream<ObservedClientSample> stream();

    int size();

    Set<ServiceRoomId> getServiceRoomIds();

    Set<UUID> getClientIds();

    Set<UUID> getPeerConnectionIds();

    Set<UUID> getMediaTrackIds();

    class Builder {
        private Set<UUID> clientIds = new HashSet<>();
        private Set<UUID> peerConnectionIds = new HashSet<>();
        private Set<UUID> mediaTrackIds = new HashSet<>();
        private Set<ServiceRoomId> serviceRoomIds = new HashSet<>();
        private List<ObservedClientSample> clientSamples = new LinkedList<>();

        public Builder addObservedClientSample(ObservedClientSample value) {
            var clientSample = value.getClientSample();
            ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                    .map(transport -> transport.peerConnectionId)
                    .forEach(peerConnectionIds::add);
            ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                    .map(track -> track.trackId)
                    .forEach(mediaTrackIds::add);
            ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                    .map(track -> track.trackId)
                    .forEach(mediaTrackIds::add);
            ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
                    .map(track -> track.trackId)
                    .forEach(mediaTrackIds::add);
            ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
                    .map(track -> track.trackId)
                    .forEach(mediaTrackIds::add);
            if (Objects.nonNull(clientSample.clientId)) {
                this.clientIds.add(clientSample.clientId);
            }
            this.serviceRoomIds.add(value.getServiceRoomId());
            this.clientSamples.add(value);
            return this;
        }

        public ObservedClientSamples build() {
            return new ObservedClientSamples() {
                @Override
                public Iterator<ObservedClientSample> iterator() {
                    return clientSamples.iterator();
                }

                @Override
                public boolean isEmpty() {
                    return clientSamples.isEmpty();
                }

                @Override
                public Stream<ObservedClientSample> stream() {
                    return clientSamples.stream();
                }

                @Override
                public int size() {
                    return clientSamples.size();
                }

                @Override
                public Set<ServiceRoomId> getServiceRoomIds() {
                    return serviceRoomIds;
                }

                @Override
                public Set<UUID> getClientIds() {
                    return clientIds;
                }

                @Override
                public Set<UUID> getPeerConnectionIds() {
                    return peerConnectionIds;
                }

                @Override
                public Set<UUID> getMediaTrackIds() {
                    return mediaTrackIds;
                }

            };
        }

    }
}
