package org.observertc.observer.samples;

import org.jetbrains.annotations.NotNull;
import org.observertc.schemas.samples.Samples;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public interface ObservedInboundVideoTrack extends Iterable<Samples.ClientSample.InboundVideoTrack> {

    String getTrackId();
    ObservedPeerConnection getObservedPeerConnection();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    Long getSSRC();
    String getMarker();
    String getSfuStreamId();
    String getSfuSinkId();

    class Builder implements ObservedInboundVideoTrack {
        private final String trackId;
        private final ObservedPeerConnection.Builder observedPeerConnection;
        private List<Samples.ClientSample.InboundVideoTrack> inboundVideoTrackSamples = new LinkedList<>();

        public Builder(ObservedPeerConnection.Builder observedPeerConnection, String trackId) {
            this.observedPeerConnection = observedPeerConnection;
            this.trackId = trackId;

            ObservedClientSamples.Builder root = this.observedPeerConnection.observedClient.observedRoom.observedClientSamples;
            root.inboundTrackIds.add(trackId);
        }

        public void add(Samples.ClientSample.InboundVideoTrack inboundAudioTrackSample) {
            this.inboundVideoTrackSamples.add(inboundAudioTrackSample);
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
            if (this.inboundVideoTrackSamples.isEmpty()) {
                return null;
            }
            return this.inboundVideoTrackSamples.get(0).ssrc;
        }

        @Override
        public String getMarker() {
            return this.observedPeerConnection.getMarker();
        }

        @Override
        public String getSfuStreamId() {
            if (this.inboundVideoTrackSamples.isEmpty()) {
                return null;
            }
            return this.inboundVideoTrackSamples.get(0).sfuStreamId;
        }

        @Override
        public String getSfuSinkId() {
            if (this.inboundVideoTrackSamples.isEmpty()) {
                return null;
            }
            return this.inboundVideoTrackSamples.get(0).sfuSinkId;
        }

        @NotNull
        @Override
        public Iterator<Samples.ClientSample.InboundVideoTrack> iterator() {
            return this.inboundVideoTrackSamples.iterator();
        }
    }
}
