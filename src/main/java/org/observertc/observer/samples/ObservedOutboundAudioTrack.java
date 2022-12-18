package org.observertc.observer.samples;

import org.jetbrains.annotations.NotNull;
import org.observertc.schemas.samples.Samples;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public interface ObservedOutboundAudioTrack extends Iterable<Samples.ClientSample.OutboundAudioTrack> {

    String getTrackId();
    ObservedPeerConnection getObservedPeerConnection();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    Long getSSRC();
    String getMarker();
    String getSfuStreamId();

    class Builder implements ObservedOutboundAudioTrack {
        private final String trackId;
        private final ObservedPeerConnection.Builder observedPeerConnection;
        private List<Samples.ClientSample.OutboundAudioTrack> outboundAudioTrackSamples = new LinkedList<>();

        public Builder(ObservedPeerConnection.Builder observedPeerConnection, String trackId) {
            this.observedPeerConnection = observedPeerConnection;
            this.trackId = trackId;

            ObservedClientSamples.Builder root = this.observedPeerConnection.observedClient.observedRoom.observedClientSamples;
            root.outboundTrackIds.add(trackId);
        }

        public void add(Samples.ClientSample.OutboundAudioTrack outboundAudioTrackSample) {
            this.outboundAudioTrackSamples.add(outboundAudioTrackSample);
        }

        @Override
        public String getTrackId() {
            return this.trackId;
        }

        @Override
        public ObservedPeerConnection getObservedPeerConnection() {
            return this.observedPeerConnection;
        }

        @Override
        public Long getMinTimestamp() {
            return this.observedPeerConnection.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return this.observedPeerConnection.getMaxTimestamp();
        }

        @Override
        public Long getSSRC() {
            if (this.outboundAudioTrackSamples.isEmpty()) {
                return null;
            }
            return this.outboundAudioTrackSamples.get(0).ssrc;
        }

        @Override
        public String getMarker() {
            return this.observedPeerConnection.getMarker();
        }

        @Override
        public String getSfuStreamId() {
            if (this.outboundAudioTrackSamples.isEmpty()) {
                return null;
            }
            return this.outboundAudioTrackSamples.get(0).sfuStreamId;
        }

        @NotNull
        @Override
        public Iterator<Samples.ClientSample.OutboundAudioTrack> iterator() {
            return this.outboundAudioTrackSamples.iterator();
        }
    }
}
