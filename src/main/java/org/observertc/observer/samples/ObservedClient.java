package org.observertc.observer.samples;

import org.jetbrains.annotations.NotNull;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ObservedClient extends Iterable<Samples.ClientSample> {

    String getClientId();
    String getMarker();
    Long getMinTimestamp();
    Long getMaxTimestamp();

    String getMediaUnitId();

    Stream<ObservedClientSample> streamObservedClientSamples();
    Iterable<ObservedClientSample> observedClientSamples();
    Iterable<ObservedPeerConnection> observedPeerConnections();

    String getUserId();
    String getTimeZoneId();


    class Builder implements ObservedClient {
        private static final Logger logger = LoggerFactory.getLogger(ObservedClient.Builder.class);

        final ObservedRoom.Builder observedRoom;
        private final String mediaUnitId;
        private final String clientId;
        private String marker = null;
        private Long minTimestamp = null;
        private Long maxTimestamp = null;
        private String timeZoneId = null;
        private List<Samples.ClientSample> clientSamples = new LinkedList<>();
        private Map<String, ObservedPeerConnection> peerConnections = new HashMap();

        Builder(ObservedRoom.Builder observedRoom, String mediaUnitId, String clientId) {
            this.observedRoom = observedRoom;
            this.mediaUnitId = mediaUnitId;
            this.clientId = clientId;

            ObservedClientSamples.Builder root = this.observedRoom.observedClientSamples;
            root.clientIds.add(clientId);
        }

        public void add(Samples.ClientSample clientSample) {
            ObservedClientSamples.Builder root = this.observedRoom.observedClientSamples;
            root.clientIds.add(clientSample.clientId);
            if (this.timeZoneId == null && clientSample.timeZoneOffsetInHours != null) {
                this.timeZoneId = root.minuteToTimeZoneOffsetConverter.apply(clientSample.timeZoneOffsetInHours);
            }

            ClientSampleVisitor.streamPeerConnectionTransports(clientSample).forEach(peerConnectionTransportSample -> {
                if (peerConnectionTransportSample.peerConnectionId == null) {
                    logger.warn("No Peer Connection Id occuured for Peer Connection Transport, clientId: {}, room: {}, service: {}",
                            this.getClientId(),
                            this.observedRoom.getServiceRoomId().roomId,
                            this.observedRoom.getServiceRoomId().serviceId
                    );
                    return;
                }

                var observedPeerConnection = (ObservedPeerConnection.Builder) this.peerConnections.get(peerConnectionTransportSample.peerConnectionId);
                if (observedPeerConnection == null) {
                    observedPeerConnection = new ObservedPeerConnection.Builder(this, peerConnectionTransportSample.peerConnectionId);
                    this.peerConnections.put(observedPeerConnection.getPeerConnectionId(), observedPeerConnection);
                }
                observedPeerConnection.addTransportSample(peerConnectionTransportSample);
            });

            ClientSampleVisitor.streamInboundAudioTracks(clientSample).forEach(inboundAudioTrackSample -> {
                if (inboundAudioTrackSample.peerConnectionId == null) {
                    logger.warn("No Peer Connection Id occuured for Inbound Audio Tracks, clientId: {}, room: {}, service: {}",
                            this.getClientId(),
                            this.observedRoom.getServiceRoomId().roomId,
                            this.observedRoom.getServiceRoomId().serviceId
                    );
                    return;
                }
                var observedPeerConnection = (ObservedPeerConnection.Builder) this.peerConnections.get(inboundAudioTrackSample.peerConnectionId);
                if (observedPeerConnection == null) {
                    observedPeerConnection = new ObservedPeerConnection.Builder(this, inboundAudioTrackSample.peerConnectionId);
                    this.peerConnections.put(observedPeerConnection.getPeerConnectionId(), observedPeerConnection);
                }
                observedPeerConnection.addInboundAudioTrack(inboundAudioTrackSample);
            });

            ClientSampleVisitor.streamInboundVideoTracks(clientSample).forEach(inboundVideoTrackSample -> {
                if (inboundVideoTrackSample.peerConnectionId == null) {
                    logger.warn("No Peer Connection Id occuured for Inbound Video Tracks, clientId: {}, room: {}, service: {}",
                            this.getClientId(),
                            this.observedRoom.getServiceRoomId().roomId,
                            this.observedRoom.getServiceRoomId().serviceId
                    );
                    return;
                }
                var observedPeerConnection = (ObservedPeerConnection.Builder) this.peerConnections.get(inboundVideoTrackSample.peerConnectionId);
                if (observedPeerConnection == null) {
                    observedPeerConnection = new ObservedPeerConnection.Builder(this, inboundVideoTrackSample.peerConnectionId);
                    this.peerConnections.put(observedPeerConnection.getPeerConnectionId(), observedPeerConnection);
                }
                observedPeerConnection.addInboundVideoTrack(inboundVideoTrackSample);
            });

            ClientSampleVisitor.streamOutboundAudioTracks(clientSample).forEach(outboundAudioTrackSample -> {
                if (outboundAudioTrackSample.peerConnectionId == null) {
                    logger.warn("No Peer Connection Id occuured for Outbound Audio Tracks, clientId: {}, room: {}, service: {}",
                            this.getClientId(),
                            this.observedRoom.getServiceRoomId().roomId,
                            this.observedRoom.getServiceRoomId().serviceId
                    );
                    return;
                }
                var observedPeerConnection = (ObservedPeerConnection.Builder) this.peerConnections.get(outboundAudioTrackSample.peerConnectionId);
                if (observedPeerConnection == null) {
                    observedPeerConnection = new ObservedPeerConnection.Builder(this, outboundAudioTrackSample.peerConnectionId);
                    this.peerConnections.put(observedPeerConnection.getPeerConnectionId(), observedPeerConnection);
                }
                observedPeerConnection.addOutboundAudioTrack(outboundAudioTrackSample);
            });

            ClientSampleVisitor.streamOutboundVideoTracks(clientSample).forEach(outboundVideoTrackSample -> {
                if (outboundVideoTrackSample.peerConnectionId == null) {
                    logger.warn("No Peer Connection Id occuured for Outbound Video Tracks, clientId: {}, room: {}, service: {}",
                            this.getClientId(),
                            this.observedRoom.getServiceRoomId().roomId,
                            this.observedRoom.getServiceRoomId().serviceId
                    );
                    return;
                }
                var observedPeerConnection = (ObservedPeerConnection.Builder) this.peerConnections.get(outboundVideoTrackSample.peerConnectionId);
                if (observedPeerConnection == null) {
                    observedPeerConnection = new ObservedPeerConnection.Builder(this, outboundVideoTrackSample.peerConnectionId);
                    this.peerConnections.put(observedPeerConnection.getPeerConnectionId(), observedPeerConnection);
                }
                observedPeerConnection.addOutboundVideoTrack(outboundVideoTrackSample);
            });

            if (this.marker == null) {
                this.marker = clientSample.marker;
            }
            if (this.minTimestamp == null || clientSample.timestamp < this.minTimestamp) {
                this.minTimestamp = clientSample.timestamp;
            }
            if (this.maxTimestamp == null || this.maxTimestamp < clientSample.timestamp) {
                this.maxTimestamp = clientSample.timestamp;
            }
            this.clientSamples.add(clientSample);
        }

        @Override
        public String getMediaUnitId() { return this.mediaUnitId; }

        @Override
        public Stream<ObservedClientSample> streamObservedClientSamples() {
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(this.observedClientSamples().iterator(), Spliterator.ORDERED),
                    false);
        }

        @Override
        public Iterable<ObservedClientSample> observedClientSamples() {
            var observedClient = this;
            return new Iterable<ObservedClientSample>() {
                @NotNull
                @Override
                public Iterator<ObservedClientSample> iterator() {
                    var it = clientSamples.iterator();

                    return new Iterator<ObservedClientSample>() {
                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public ObservedClientSample next() {
                            var clientSample = it.next();
                            return new ObservedClientSample() {
                                @Override
                                public String getServiceId() {
                                    return observedClient.observedRoom.getServiceRoomId().serviceId;
                                }

                                @Override
                                public ServiceRoomId getServiceRoomId() {
                                    return observedClient.observedRoom.getServiceRoomId();
                                }

                                @Override
                                public String getMediaUnitId() {
                                    return observedClient.getMediaUnitId();
                                }

                                @Override
                                public String getTimeZoneId() {
                                    return timeZoneId;
                                }

                                @Override
                                public Samples.ClientSample getClientSample() {
                                    return clientSample;
                                }
                            };
                        }
                    };
                }
            };
        }

        @Override
        public String getClientId() {
            return this.clientId;
        }

        @Override
        public String getMarker() {
            return this.marker;
        }

        @Override
        public Long getMinTimestamp() {
            return this.minTimestamp;
        }

        @Override
        public Long getMaxTimestamp() {
            return this.maxTimestamp;
        }

        @Override
        public Iterable<ObservedPeerConnection> observedPeerConnections() {
            return () -> this.peerConnections.values().iterator();
        }

        @Override
        public String getUserId() {
            if (this.clientSamples.isEmpty()) {
                return null;
            }
            return this.clientSamples.get(0).userId;
        }

        @Override
        public String getTimeZoneId() {
            return this.timeZoneId;
        }

        @NotNull
        @Override
        public Iterator<Samples.ClientSample> iterator() {
            return this.clientSamples.iterator();
        }
    }

}
