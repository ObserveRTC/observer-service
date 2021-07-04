package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.common.UUIDAdapter;

import javax.validation.constraints.NotNull;
import java.io.InvalidObjectException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ObservedClientSampleBuilder {

    public static ObservedClientSampleBuilder from(ClientSample clientSample) {
        return new ObservedClientSampleBuilder().withClientSample(clientSample);
    }

    private String serviceId;
    private String mediaUnitId;
    private ClientSample clientSample;
    private UUID clientId;
    private String timeZoneId;

    private ObservedClientSampleBuilder() {

    }

    public ObservedClientSampleBuilder withServiceId(String value) {
        this.serviceId = value;
        return this;
    }

    public ObservedClientSampleBuilder withMediaUnitId(String value) {
        this.mediaUnitId = value;
        return this;
    }

    private ObservedClientSampleBuilder withClientSample(ClientSample value) {
        this.clientSample = value;
        this.clientId = UUID.fromString(value.clientId);
        return this;
    }

    public ObservedClientSampleBuilder withTimeZoneId(String value) {
        this.timeZoneId = value;
        return this;
    }

    public boolean isValid(@NotNull AtomicReference<String> message) {
        if (Objects.isNull(this.serviceId)) {
            message.set("serviceId is null");
            return false;
        }
        if (Objects.isNull(this.clientSample)) {
            message.set("ClientSample is null");
            return false;
        }
        if (Objects.isNull(this.clientSample.clientId)) {
            message.set("clientId in client sample is null");
            return false;
        }
        if (UUIDAdapter.tryParse(this.clientSample.clientId).isEmpty()) {
            message.set("clientId in client sample is not a valid uuid");
            return false;
        }
        if (Objects.isNull(this.clientSample.timestamp)) {
            message.set("ClientSample.timestamp is null");
            return false;
        }
        if (Objects.isNull(this.clientSample.roomId)) {
            message.set("ClientSample.roomId is null");
            return false;
        }

        Set<UUID> peerConnectionIds = ClientSampleVisitor.streamPeerConnectionTransports(this.clientSample)
                .map(t -> UUIDAdapter.tryParse(t.peerConnectionId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        boolean inbAOk = ClientSampleVisitor.streamInboundAudioTracks(this.clientSample).allMatch(t ->
                Objects.nonNull(t.trackId) &&
                        Objects.nonNull(t.peerConnectionId) &&
                        UUIDAdapter.tryParse(t.trackId).isPresent() &&
                        peerConnectionIds.contains(UUID.fromString(t.peerConnectionId)) &&
                        Objects.nonNull(t.ssrc));
        if (!inbAOk) {
            message.set("All Inbound Audio Tracks must have SSRC and valid UUID for trackId, peerConnectionId. For peerConnectionId the corresponding pcTransport must be presented");
            return false;
        }
        boolean inbVOk = ClientSampleVisitor.streamInboundVideoTracks(this.clientSample).allMatch(t ->
                Objects.nonNull(t.trackId) &&
                        Objects.nonNull(t.peerConnectionId) &&
                        UUIDAdapter.tryParse(t.trackId).isPresent() &&
                        peerConnectionIds.contains(UUID.fromString(t.peerConnectionId)) &&
                        Objects.nonNull(t.ssrc));
        if (!inbVOk) {
            message.set("All Inbound Video Tracks must have SSRC and valid UUID for trackId, peerConnectionId. For peerConnectionId the corresponding pcTransport must be presented");
            return false;
        }
        boolean outbAOk = ClientSampleVisitor.streamOutboundAudioTracks(this.clientSample).allMatch(t ->
                Objects.nonNull(t.trackId) &&
                        Objects.nonNull(t.peerConnectionId) &&
                        UUIDAdapter.tryParse(t.trackId).isPresent() &&
                        peerConnectionIds.contains(UUID.fromString(t.peerConnectionId)) &&
                        Objects.nonNull(t.ssrc));
        if (!outbAOk) {
            message.set("All Outbound Audio Tracks must have SSRC and valid UUID for trackId, peerConnectionId. For peerConnectionId the corresponding pcTransport must be presented");
            return false;
        }
        boolean outbVok = ClientSampleVisitor.streamOutboundVideoTracks(this.clientSample).allMatch(t ->
                Objects.nonNull(t.trackId) &&
                        Objects.nonNull(t.peerConnectionId) &&
                        UUIDAdapter.tryParse(t.trackId).isPresent() &&
                        peerConnectionIds.contains(UUID.fromString(t.peerConnectionId)) &&
                        Objects.nonNull(t.ssrc));
        if (!outbVok) {
            message.set("All Outbound Video Tracks must have SSRC and valid UUID for trackId, peerConnectionId. For peerConnectionId the corresponding pcTransport must be presented");
            return false;
        }

        return true;
    }

    public ObservedClientSample build() throws InvalidObjectException {
        AtomicReference<String> message = new AtomicReference<>(null);
        if (!this.isValid(message)) {
            throw new InvalidObjectException(message.get());
        }

        return new ObservedClientSample() {
            @Override
            public String getServiceId() {
                return serviceId;
            }

            @Override
            public String getMediaUnitId() {
                return mediaUnitId;
            }

            @Override
            public UUID getClientId() {
                return clientId;
            }

            @Override
            public ClientSample getClientSample() {
                return clientSample;
            }

            @Override
            public String getUserId() {
                return clientSample.userId;
            }

            @Override
            public int getSampleSeq() {
                return clientSample.sampleSeq;
            }

            @Override
            public String getMarker() {
                return clientSample.marker;
            }

            @Override
            public String getTimeZoneId() {
                return timeZoneId;
            }

            @Override
            public Long getTimestamp() {
                return clientSample.timestamp;
            }

            @Override
            public String getRoomId() {
                return clientSample.roomId;
            }


        };
    }
}
