package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface ObservedPeerConnection {
    ObservedClient getObservedClient();

    String getPeerConnectionId();
    String getMarker();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    Iterable<ObservedInboundAudioTrack> observedInboundAudioTracks();
    Iterable<ObservedInboundVideoTrack> observedInboundVideoTracks();

    Iterable<ObservedOutboundAudioTrack> observedOutboundAudioTracks();
    Iterable<ObservedOutboundVideoTrack> observedOutboundVideoTracks();


    class Builder implements ObservedPeerConnection {
        private static final Logger logger = LoggerFactory.getLogger(ObservedPeerConnection.Builder.class);

        final ObservedClient.Builder observedClient;
        private final String peerConnectionId;
        private List<Samples.ClientSample.PeerConnectionTransport> transportSamples = new LinkedList<>();
        private Map<String, ObservedInboundAudioTrack> inboundAudioTracks = new HashMap<>();
        private Map<String, ObservedInboundVideoTrack> inboundVideoTracks = new HashMap<>();
        private Map<String, ObservedOutboundAudioTrack> outboundAudioTracks = new HashMap<>();
        private Map<String, ObservedOutboundVideoTrack> outboundVideoTracks = new HashMap<>();


        Builder(ObservedClient.Builder observedClient, String peerConnectionId) {
            this.observedClient = observedClient;
            this.peerConnectionId = peerConnectionId;

            ObservedClientSamples.Builder root = this.observedClient.observedRoom.observedClientSamples;
            root.peerConnectionIds.add(peerConnectionId);
        }

        public void addTransportSample(Samples.ClientSample.PeerConnectionTransport transportSample) {
            this.transportSamples.add(transportSample);
        }

        public void addInboundAudioTrack(Samples.ClientSample.InboundAudioTrack inboundAudioTrackSample) {
            if (inboundAudioTrackSample.trackId == null) {
                logger.warn("No TrackId for Inbound Audio Track occurred for peer connection {}, clientId: {}, room: {}, service: {}",
                        this.peerConnectionId,
                        this.observedClient.getClientId(),
                        this.observedClient.observedRoom.getServiceRoomId().roomId,
                        this.observedClient.observedRoom.getServiceRoomId().serviceId
                );
                return;
            }
            ObservedInboundAudioTrack.Builder observedInboundAudioTrack = (ObservedInboundAudioTrack.Builder) this.inboundAudioTracks.get(inboundAudioTrackSample.trackId);
            if (observedInboundAudioTrack == null) {
                observedInboundAudioTrack = new ObservedInboundAudioTrack.Builder(this, inboundAudioTrackSample.trackId);
                this.inboundAudioTracks.put(observedInboundAudioTrack.getTrackId(), observedInboundAudioTrack);
            }
            observedInboundAudioTrack.add(inboundAudioTrackSample);
        }

        public void addInboundVideoTrack(Samples.ClientSample.InboundVideoTrack inboundVideoTrackSample) {
            if (inboundVideoTrackSample.trackId == null) {
                logger.warn("No TrackId for Inbound Video Track occurred for peer connection {}, clientId: {}, room: {}, service: {}",
                        this.peerConnectionId,
                        this.observedClient.getClientId(),
                        this.observedClient.observedRoom.getServiceRoomId().roomId,
                        this.observedClient.observedRoom.getServiceRoomId().serviceId
                );
                return;
            }
            ObservedInboundVideoTrack.Builder observedInboundVideoTrack = (ObservedInboundVideoTrack.Builder) this.inboundVideoTracks.get(inboundVideoTrackSample.trackId);
            if (observedInboundVideoTrack == null) {
                observedInboundVideoTrack = new ObservedInboundVideoTrack.Builder(this, inboundVideoTrackSample.trackId);
                this.inboundVideoTracks.put(observedInboundVideoTrack.getTrackId(), observedInboundVideoTrack);
            }
            observedInboundVideoTrack.add(inboundVideoTrackSample);
        }

        public void addOutboundAudioTrack(Samples.ClientSample.OutboundAudioTrack outboundAudioTrackSample) {
            if (outboundAudioTrackSample.trackId == null) {
                logger.warn("No TrackId for Outbound Audio Track occurred for peer connection {}, clientId: {}, room: {}, service: {}",
                        this.peerConnectionId,
                        this.observedClient.getClientId(),
                        this.observedClient.observedRoom.getServiceRoomId().roomId,
                        this.observedClient.observedRoom.getServiceRoomId().serviceId
                );
                return;
            }
            ObservedOutboundAudioTrack.Builder observedOutboundAudioTrack = (ObservedOutboundAudioTrack.Builder) this.outboundAudioTracks.get(outboundAudioTrackSample.trackId);
            if (observedOutboundAudioTrack == null) {
                observedOutboundAudioTrack = new ObservedOutboundAudioTrack.Builder(this, outboundAudioTrackSample.trackId);
                this.outboundAudioTracks.put(observedOutboundAudioTrack.getTrackId(), observedOutboundAudioTrack);
            }
            observedOutboundAudioTrack.add(outboundAudioTrackSample);
        }

        public void addOutboundVideoTrack(Samples.ClientSample.OutboundVideoTrack outboundVideoTrackSample) {
            if (outboundVideoTrackSample.trackId == null) {
                logger.warn("No TrackId for Outbound Video Track occurred for peer connection {}, clientId: {}, room: {}, service: {}",
                        this.peerConnectionId,
                        this.observedClient.getClientId(),
                        this.observedClient.observedRoom.getServiceRoomId().roomId,
                        this.observedClient.observedRoom.getServiceRoomId().serviceId
                );
                return;
            }
            ObservedOutboundVideoTrack.Builder observedOutboundVideoTrack = (ObservedOutboundVideoTrack.Builder) this.outboundVideoTracks.get(outboundVideoTrackSample.trackId);
            if (observedOutboundVideoTrack == null) {
                observedOutboundVideoTrack = new ObservedOutboundVideoTrack.Builder(this, outboundVideoTrackSample.trackId);
                this.outboundVideoTracks.put(observedOutboundVideoTrack.getTrackId(), observedOutboundVideoTrack);
            }
            observedOutboundVideoTrack.add(outboundVideoTrackSample);
        }

        @Override
        public ObservedClient getObservedClient() {
            return this.observedClient;
        }

        @Override
        public String getPeerConnectionId() {
            return this.peerConnectionId;
        }

        @Override
        public String getMarker() {
            return this.observedClient.getMarker();
        }

        @Override
        public Long getMinTimestamp() {
            return this.observedClient.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return this.observedClient.getMaxTimestamp();
        }

        @Override
        public Iterable<ObservedInboundAudioTrack> observedInboundAudioTracks() {
            return () -> this.inboundAudioTracks.values().iterator();
        }

        @Override
        public Iterable<ObservedInboundVideoTrack> observedInboundVideoTracks() {
            return () -> this.inboundVideoTracks.values().iterator();
        }

        @Override
        public Iterable<ObservedOutboundAudioTrack> observedOutboundAudioTracks() {
            return () -> this.outboundAudioTracks.values().iterator();
        }

        @Override
        public Iterable<ObservedOutboundVideoTrack> observedOutboundVideoTracks() {
            return () -> this.outboundVideoTracks.values().iterator();
        }

    }
}
