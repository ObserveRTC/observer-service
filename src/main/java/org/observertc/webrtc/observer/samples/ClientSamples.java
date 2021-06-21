package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.common.UUIDAdapter;

import java.util.*;
import java.util.stream.Stream;

/**
 * Call assigned and organized ObservedSamples
 */
public class ClientSamples implements ObservedSample, Iterable<ClientSample> {

    public static ClientSamples.Builder builderFrom(ObservedSample observedSample) {
        var result = new ClientSamples.Builder()
                .withObservedSample(observedSample);
        return result;
    }
    private ObservedSample observedSample;
    private ServiceRoomId serviceRoomId;
    private List<ClientSample> samples = new LinkedList<>();
    private Set<MediaTrackId> inboundMediaTrackIds = new HashSet<>();
    private Long minTimestamp = null;
    private ClientSamples() {

    }

    public Stream<ClientSample> stream() {
        return this.samples.stream();
    }

    void add(ClientSample clientSample) {
        Objects.requireNonNull(clientSample);
        this.samples.add(clientSample);
        if (Objects.nonNull(clientSample.timestamp)) {
            if (Objects.isNull(this.minTimestamp) || clientSample.timestamp < this.minTimestamp) {
                this.minTimestamp = clientSample.timestamp;
            }
        }
        ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                .filter(track -> Objects.nonNull(track.ssrc) && UUIDAdapter.tryParse(track.peerConnectionId).isPresent())
                .map(track -> MediaTrackId.make(UUID.fromString(track.peerConnectionId), track.ssrc))
                .forEach(this.inboundMediaTrackIds::add);

        ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                .filter(track -> Objects.nonNull(track.ssrc) && UUIDAdapter.tryParse(track.peerConnectionId).isPresent())
                .map(track -> MediaTrackId.make(UUID.fromString(track.peerConnectionId), track.ssrc))
                .forEach(this.inboundMediaTrackIds::add);
    }

    void addAll(ClientSamples clientSamples) {
        Objects.requireNonNull(clientSamples);
        clientSamples.stream().forEach(this::add);
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public Set<MediaTrackId> getInboundMediaTrackIds() {
        return this.inboundMediaTrackIds;
    }

    public Long getMinTimestamp() {
        return this.minTimestamp;
    }

    @Override
    public String getServiceId() {
        return this.observedSample.getServiceId();
    }

    @Override
    public String getMediaUnitId() {
        return this.observedSample.getMediaUnitId();
    }

    @Override
    public UUID getClientId() {
        return this.observedSample.getClientId();
    }

    @Override
    public String getTimeZoneId() {
        return this.observedSample.getTimeZoneId();
    }

    @Override
    public Long getTimestamp() {
        return this.observedSample.getTimestamp();
    }

    @Override
    public String getRoomId() {
        return this.observedSample.getRoomId();
    }

    @Override
    public Iterator<ClientSample> iterator() {
        return this.samples.iterator();
    }

    public String getUserId() {
        if (this.samples.size() < 1) {
            return null;
        }
        var firstSample = this.samples.get(0);
        return firstSample.userId;
    }

    public String getMarker() {
        if (this.samples.size() < 1) {
            return null;
        }
        var firstSample = this.samples.get(0);
        // TODO: add marker to ClientSample
        return "NOT IMPLEMENTED";
    }

    public static class Builder {
        private ClientSamples result;

        public Builder withClientSample(ClientSample clientSample) {
            Objects.requireNonNull(clientSample);
            this.result.add(clientSample);
            return this;
        }

        Builder withObservedSample(ObservedSample observedSample) {
            this.result.observedSample = observedSample;
            this.result.serviceRoomId = ServiceRoomId.make(
                    observedSample.getServiceId(),
                    observedSample.getRoomId()
            );
            return this;
        }

        public ClientSamples build() {
            return this.result;
        }
    }
}
