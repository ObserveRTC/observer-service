package org.observertc.observer.utils;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.dto.*;

import javax.inject.Inject;
import java.util.UUID;

@Prototype
public class DTOGenerators {

    @Inject
    RandomGenerators randomGenerators;


    public DTOGenerators() {

    }

    public CallDTO getCallDTO() {
        var result = this.getCallDTOBuilder()
                .build();
        return result;
    }

    public CallDTO.Builder getCallDTOBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var result = CallDTO.builder()
                .withServiceId(serviceId)
                .withRoomId(roomId)
                .withCallId(callId)
                .withStartedTimestamp(timestamp)
                ;
        return result;
    }

    public ClientDTO getClientDTO() {
        var result = this.getClientDTOBuilder()
                .build();
        return result;
    }

    public ClientDTO.Builder getClientDTOBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID();
        var userId = this.randomGenerators.getRandomTestUserIds();
        var timeZoneId = this.randomGenerators.getRandomTimeZoneId();
        var result = ClientDTO.builder()
                .withServiceId(serviceId)
                .withRoomId(roomId)
                .withCallId(callId)
                .withMediaUnitId(mediaUnitId)
                .withClientId(clientId)
                .withUserId(userId)
                .withJoinedTimestamp(timestamp)
                .withTimeZoneId(timeZoneId);
        return result;
    }

    public PeerConnectionDTO getPeerConnectionDTO() {
        var result = this.getPeerConnectionDTOBuilder()
                .build()
                ;
        return result;
    }

    public PeerConnectionDTO.Builder getPeerConnectionDTOBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        var userId = this.randomGenerators.getRandomTestUserIds();
        var result = PeerConnectionDTO.builder()
                .withServiceId(serviceId)
                .withRoomId(roomId)
                .withCallId(callId)
                .withMediaUnitId(mediaUnitId)
                .withClientId(clientId)
                .withPeerConnectionId(peerConnectionId)
                .withUserId(userId)
                .withCreatedTimestamp(timestamp)
                ;
        return result;
    }

    public MediaTrackDTO getMediaTrackDTO() {
        var result = this.getMediaTrackDTOBuilder().build();
        return result;
    }

    public MediaTrackDTO.Builder getMediaTrackDTOBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        var userId = this.randomGenerators.getRandomTestUserIds();
        var ssrc = this.randomGenerators.getRandomSSRC();
        var sfuStreamId = callId.getLeastSignificantBits() % 2L == 0 ? UUID.randomUUID() : null;
        var direction = this.randomGenerators.getRandomStreamDirection();
        var result = MediaTrackDTO.builder()
                .withServiceId(serviceId)
                .withRoomId(roomId)
                .withCallId(callId)
                .withMediaUnitId(mediaUnitId)
                .withClientId(clientId)
                .withPeerConnectionId(peerConnectionId)
                .withTrackId(trackId)
                .withUserId(userId)
                .withAddedTimestamp(timestamp)
                .withSSRC(ssrc)
                .withSfuStreamId(sfuStreamId)
                .withDirection(direction)
                ;
        return result;
    }

    public SfuDTO getSfuDTO() {
        var result = this.getSfuDTOBuilder().build();
        return result;
    }

    public SfuDTO.Builder getSfuDTOBuilder() {
        var sfuId = UUID.randomUUID();
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var timeZoneId = this.randomGenerators.getRandomTimeZoneId();
        var result = SfuDTO.builder()
                .withSfuId(sfuId)
                .withServiceId(serviceId)
                .withMediaUnitId(mediaUnitId)
                .withConnectedTimestamp(timestamp)
                .withTimeZoneId(timeZoneId)
                ;
        return result;
    }

    public SfuTransportDTO getSfuTransportDTO() {
        var sfuId = UUID.randomUUID();
        var callId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var internal = this.randomGenerators.getRandomPort() % 2 == 0;
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var result = SfuTransportDTO.builder()
                .withSfuId(sfuId)
                .withServiceId(serviceId)
                .withInternal(internal)
                .withTransportId(transportId)
                .withCallId(callId)
                .withMediaUnitId(mediaUnitId)
                .withOpenedTimestamp(timestamp)
                .build();
        return result;
    }

    public SfuRtpPadDTO getSfuRtpPadDTO() {
        var sfuId = UUID.randomUUID();
        var callId = UUID.randomUUID();
        var sfuStreamId = UUID.randomUUID();
        var sfuPadId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var sfuTransportId = UUID.randomUUID();
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var streamDirection = this.randomGenerators.getRandomStreamDirection();
        var result = SfuRtpPadDTO.builder()
                .withServiceId(serviceId)
                .withMediaUnitId(mediaUnitId)
                .withSfuId(sfuId)
                .withSfuTransportId(sfuTransportId)
                .withStreamId(sfuStreamId)
                .withSfuRtpPadId(sfuPadId)
                .withStreamDirection(streamDirection)
                .withAddedTimestamp(timestamp)
                .build();
        return result;
    }
}
