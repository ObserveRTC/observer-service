package org.observertc.observer.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public interface ObservedClientSamples extends Iterable<ObservedClientSample> {
    static final Logger logger = LoggerFactory.getLogger(ObservedClientSamples.class);

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

        // compile this below if you want to validate all the inputs
//        public Builder addObservedClientSample(ObservedClientSample value) {
//            var clientSample = value.getClientSample();
//            var nullPeerConnectionIds = new HashSet<UUID>();
//            var nullInboundAudioTrackIds = new HashSet<UUID>();
//            var nullInboundVideoTrackIds = new HashSet<UUID>();
//            var nullOutboundAudioTrackIds = new HashSet<UUID>();
//            var nullOutboundVideoTrackIds = new HashSet<UUID>();
//            var peerConnectionIdFilter = Utils.makeTrash(Objects::nonNull, nullPeerConnectionIds);
//            var inboundAudioTrackIdFilter = Utils.makeTrash(Objects::nonNull, nullInboundAudioTrackIds);
//            var inboundVideoTrackIdFilter = Utils.makeTrash(Objects::nonNull, nullInboundVideoTrackIds);
//            var outboundAudioTrackIdFilter = Utils.makeTrash(Objects::nonNull, nullOutboundAudioTrackIds);
//            var outboundVideoTrackIdFilter = Utils.makeTrash(Objects::nonNull, nullOutboundVideoTrackIds);
//            ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
//                .map(transport -> transport.peerConnectionId)
//                .filter(peerConnectionIdFilter)
//                .forEach(peerConnectionIds::add);
//            ClientSampleVisitor.streamInboundAudioTracks(clientSample)
//                    .map(track -> track.trackId)
//                    .filter(inboundAudioTrackIdFilter)
//                    .forEach(mediaTrackIds::add);
//            ClientSampleVisitor.streamInboundVideoTracks(clientSample)
//                    .map(track -> track.trackId)
//                    .filter(inboundVideoTrackIdFilter)
//                    .forEach(mediaTrackIds::add);
//            ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
//                    .map(track -> track.trackId)
//                    .filter(outboundAudioTrackIdFilter)
//                    .forEach(mediaTrackIds::add);
//            ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
//                    .map(track -> track.trackId)
//                    .filter(outboundVideoTrackIdFilter)
//                    .forEach(mediaTrackIds::add);
//            if (Objects.nonNull(clientSample.clientId)) {
//                this.clientIds.add(clientSample.clientId);
//            }
//            if (0 < nullPeerConnectionIds.size()) {
//                logger.warn("In service {} at room {}, client: {}, userId: {} reported a sample with null peer connectionIds",
//                        value.getServiceId(),
//                        clientSample.roomId,
//                        clientSample.clientId,
//                        clientSample.userId);
//            }
//            if (0 < nullInboundAudioTrackIds.size()) {
//                logger.warn("In service {} at room {}, client: {}, userId: {} reported a sample with null inbound audio track ids",
//                        value.getServiceId(),
//                        clientSample.roomId,
//                        clientSample.clientId,
//                        clientSample.userId);
//            }
//            if (0 < nullInboundVideoTrackIds.size()) {
//                logger.warn("In service {} at room {}, client: {}, userId: {} reported a sample with null inbound video track ids",
//                        value.getServiceId(),
//                        clientSample.roomId,
//                        clientSample.clientId,
//                        clientSample.userId);
//            }
//            if (0 < nullOutboundAudioTrackIds.size()) {
//                logger.warn("In service {} at room {}, client: {}, userId: {} reported a sample with null outbound audio track ids",
//                        value.getServiceId(),
//                        clientSample.roomId,
//                        clientSample.clientId,
//                        clientSample.userId);
//            }
//            if (0 < nullOutboundVideoTrackIds.size()) {
//                logger.warn("In service {} at room {}, client: {}, userId: {} reported a sample with null outbound video track ids",
//                        value.getServiceId(),
//                        clientSample.roomId,
//                        clientSample.clientId,
//                        clientSample.userId);
//            }
//            this.serviceRoomIds.add(value.getServiceRoomId());
//            this.clientSamples.add(value);
//            return this;
//        }

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
