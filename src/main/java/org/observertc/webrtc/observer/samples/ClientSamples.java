package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.common.UUIDAdapter;

import java.util.*;
import java.util.stream.Stream;

/**
 * Call assigned and organized ObservedSamples
 */
public class ClientSamples implements ObservedClientSample, Iterable<ClientSample> {

    public static ClientSamples.Builder builderFrom(ObservedClientSample observedSample) {
        var result = new ClientSamples.Builder()
                .withObservedSample(observedSample);
        return result;
    }
    private ObservedClientSample observedClientSample;
    private ServiceRoomId serviceRoomId;
    private List<ClientSample> samples = new LinkedList<>();
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Set<UUID> inboundMediaTrackIds = new HashSet<>();
    private Set<UUID> outboundMediaTrackIds = new HashSet<>();
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
        ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                .map(t -> UUID.fromString(t.peerConnectionId))
                .forEach(this.peerConnectionIds::add);

        ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                .filter(track -> Objects.nonNull(track.ssrc) && UUIDAdapter.tryParse(track.peerConnectionId).isPresent())
                .map(track -> {
                    UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                    this.peerConnectionIds.add(peerConnectionId);
                    return UUID.fromString(track.trackId);
                })
                .forEach(this.inboundMediaTrackIds::add);

        ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                .filter(track -> Objects.nonNull(track.ssrc) && UUIDAdapter.tryParse(track.peerConnectionId).isPresent())
                .map(track -> {
                    UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                    this.peerConnectionIds.add(peerConnectionId);
                    return UUID.fromString(track.trackId);
                })
                .forEach(this.inboundMediaTrackIds::add);

        ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
                .filter(track -> Objects.nonNull(track.ssrc) && UUIDAdapter.tryParse(track.peerConnectionId).isPresent())
                .map(track -> {
                    UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                    this.peerConnectionIds.add(peerConnectionId);
                    return UUID.fromString(track.trackId);
                })
                .forEach(this.outboundMediaTrackIds::add);

        ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
                .filter(track -> Objects.nonNull(track.ssrc) && UUIDAdapter.tryParse(track.peerConnectionId).isPresent())
                .map(track -> {
                    UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                    this.peerConnectionIds.add(peerConnectionId);
                    return UUID.fromString(track.trackId);
                })
                .forEach(this.outboundMediaTrackIds::add);
    }

    void addAll(ClientSamples clientSamples) {
        Objects.requireNonNull(clientSamples);
        clientSamples.stream().forEach(this::add);
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public Set<UUID> getInboundMediaTrackIds() {
        return this.inboundMediaTrackIds;
    }

    public Set<UUID> getOutboundMediaTrackIds() {
        return this.outboundMediaTrackIds;
    }

    public Set<UUID> getPeerConnectionIds() {
        return this.peerConnectionIds;
    }

    public Long getMinTimestamp() {
        return this.minTimestamp;
    }

    @Override
    public String getServiceId() {
        return this.observedClientSample.getServiceId();
    }

    @Override
    public String getMediaUnitId() {
        return this.observedClientSample.getMediaUnitId();
    }

    @Override
    public UUID getClientId() {
        return this.observedClientSample.getClientId();
    }

    @Override
    public ClientSample getClientSample() {
        return this.observedClientSample.getClientSample();
    }

    @Override
    public String getTimeZoneId() {
        return this.observedClientSample.getTimeZoneId();
    }

    @Override
    public Long getTimestamp() {
        return this.observedClientSample.getTimestamp();
    }

    @Override
    public String getRoomId() {
        return this.observedClientSample.getRoomId();
    }

    @Override
    public Iterator<ClientSample> iterator() {
        return this.samples.iterator();
    }

    public String getUserId() {
        return this.observedClientSample.getUserId();
    }

    @Override
    public int getSampleSeq() {
        return this.observedClientSample.getSampleSeq();
    }

    public String getMarker() {
        return this.observedClientSample.getMarker();
    }



    public static class Builder {
        private final ClientSamples result = new ClientSamples();

        public Builder withClientSample(ClientSample clientSample) {
            Objects.requireNonNull(clientSample);
            this.result.add(clientSample);
            return this;
        }

        Builder withObservedSample(ObservedClientSample observedClientSample) {
            this.result.observedClientSample = observedClientSample;
            this.result.serviceRoomId = ServiceRoomId.make(
                    observedClientSample.getServiceId(),
                    observedClientSample.getRoomId()
            );
            return this;
        }

        public ClientSamples build() {
            return this.result;
        }
    }
}
