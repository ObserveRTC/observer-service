package org.observertc.observer.utils;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.dto.*;

import java.util.UUID;

@Prototype
public class DTOGenerators {

    RandomGenerators randomGenerators = new RandomGenerators();


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
        var marker = this.randomGenerators.getRandomMarker();
        var result = CallDTO.builder()
                .withServiceId(serviceId)
                .withRoomId(roomId)
                .withCallId(callId)
                .withStartedTimestamp(timestamp)
                .withMarker(marker)
                ;
        return result;
    }

    public ClientDTO getClientDTO() {
        var result = this.getClientDTOBuilder()
                .build();
        return result;
    }

    public ClientDTO getClientDTOFromCallDTO(CallDTO callDTO) {
        var result = this.getClientDTOBuilderFromCallDTO(callDTO)
                .build()
                ;
        return result;
    }

    public ClientDTO.Builder getClientDTOBuilderFromCallDTO(CallDTO callDTO) {
        var result = this.getClientDTOBuilder()
                .withCallId(callDTO.callId)
                .withServiceId(callDTO.serviceId)
                .withRoomId(callDTO.roomId)
                ;
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
        var marker = this.randomGenerators.getRandomMarker();
        var result = ClientDTO.builder()
                .withServiceId(serviceId)
                .withRoomId(roomId)
                .withCallId(callId)
                .withMediaUnitId(mediaUnitId)
                .withClientId(clientId)
                .withUserId(userId)
                .withJoinedTimestamp(timestamp)
                .withTimeZoneId(timeZoneId)
                .withMarker(marker)
                ;
        return result;
    }

    public PeerConnectionDTO getPeerConnectionDTO() {
        var result = this.getPeerConnectionDTOBuilder()
                .build()
                ;
        return result;
    }

    public PeerConnectionDTO getPeerConnectionDTOFromClientDTO(ClientDTO clientDTO) {
        var result = this.getPeerConnectionDTOBuilderFromClientDTO(clientDTO)
                .build()
                ;
        return result;
    }

    public PeerConnectionDTO.Builder getPeerConnectionDTOBuilderFromClientDTO(ClientDTO clientDTO) {
        var result = this.getPeerConnectionDTOBuilder()
                .withCallId(clientDTO.callId)
                .withServiceId(clientDTO.serviceId)
                .withRoomId(clientDTO.roomId)
                .withMediaUnitId(clientDTO.mediaUnitId)
                .withClientId(clientDTO.clientId)
                .withUserId(clientDTO.userId)
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
        var marker = this.randomGenerators.getRandomMarker();
        var result = PeerConnectionDTO.builder()
                .withServiceId(serviceId)
                .withRoomId(roomId)
                .withCallId(callId)
                .withMediaUnitId(mediaUnitId)
                .withClientId(clientId)
                .withPeerConnectionId(peerConnectionId)
                .withUserId(userId)
                .withCreatedTimestamp(timestamp)
                .withMarker(marker)
                ;
        return result;
    }

    public MediaTrackDTO getMediaTrackDTO() {
        var result = this.getMediaTrackDTOBuilder().build();
        return result;
    }

    public MediaTrackDTO getMediaTrackDTOFromPeerConnectionDTO(PeerConnectionDTO peerConnectionDTO) {
        var result = this.getMediaTrackDTOBuilderFromPeerConnectionDTO(peerConnectionDTO)
                .build()
                ;
        return result;
    }

    public MediaTrackDTO.Builder getMediaTrackDTOBuilderFromPeerConnectionDTO(PeerConnectionDTO peerConnectionDTO) {
        var result = this.getMediaTrackDTOBuilder()
                .withCallId(peerConnectionDTO.callId)
                .withServiceId(peerConnectionDTO.serviceId)
                .withRoomId(peerConnectionDTO.roomId)
                .withMediaUnitId(peerConnectionDTO.mediaUnitId)
                .withClientId(peerConnectionDTO.clientId)
                .withUserId(peerConnectionDTO.userId)
                .withPeerConnectionId(peerConnectionDTO.peerConnectionId)
                ;
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
        var marker = this.randomGenerators.getRandomMarker();
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
                .withMarker(marker)
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
        var marker = this.randomGenerators.getRandomMarker();
        var result = SfuDTO.builder()
                .withSfuId(sfuId)
                .withServiceId(serviceId)
                .withMediaUnitId(mediaUnitId)
                .withConnectedTimestamp(timestamp)
                .withTimeZoneId(timeZoneId)
                .withMarker(marker)
                ;
        return result;
    }

    public SfuTransportDTO getSfuTransportDTOFromSfuDTO(SfuDTO sfuDTO) {
        var result = this.getSfuTransportDTOBuilderFromSfuDTO(sfuDTO)
                .build()
                ;
        return result;
    }

    public SfuTransportDTO.Builder getSfuTransportDTOBuilderFromSfuDTO(SfuDTO sfuDTO) {
        var result = this.getSfuTransportDTOBuilder()
                .withSfuId(sfuDTO.sfuId)
                .withServiceId(sfuDTO.serviceId)
                .withMediaUnitId(sfuDTO.mediaUnitId)
                ;
        return result;
    }

    public SfuTransportDTO getSfuTransportDTO() {
        var result = this.getSfuTransportDTOBuilder()
                .build();
        return result;
    }

    public SfuTransportDTO.Builder getSfuTransportDTOBuilder() {
        var sfuId = UUID.randomUUID();
        var callId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var internal = this.randomGenerators.getRandomPort() % 2 == 0;
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var marker = this.randomGenerators.getRandomMarker();
        var result = SfuTransportDTO.builder()
                .withSfuId(sfuId)
                .withServiceId(serviceId)
                .withInternal(internal)
                .withTransportId(transportId)
                .withMediaUnitId(mediaUnitId)
                .withOpenedTimestamp(timestamp)
                .withMarker(marker)
                ;
        return result;
    }

    public SfuRtpPadDTO getSfuRtpPadDTO() {
        var result = this.getSfuRtpPadDTOBuilder()
                .build()
                ;
        return result;
    }

    public SfuRtpPadDTO getSfuRtpPadDTOFromSfuTransportDTO(SfuTransportDTO sfuTransportDTO) {
        var result = this.getSfuRtpPadDTOBuilderFromSfuTransportDTO(sfuTransportDTO)
                .build()
                ;
        return result;
    }

    public SfuRtpPadDTO.Builder getSfuRtpPadDTOBuilderFromSfuTransportDTO(SfuTransportDTO sfuTransportDTO) {
        var result = this.getSfuRtpPadDTOBuilder()
                .withSfuId(sfuTransportDTO.sfuId)
                .withServiceId(sfuTransportDTO.serviceId)
                .withMediaUnitId(sfuTransportDTO.mediaUnitId)
                .withSfuTransportId(sfuTransportDTO.transportId)
                .withInternal(sfuTransportDTO.internal)
                ;
        return result;
    }

    public SfuRtpPadDTO.Builder getSfuRtpPadDTOBuilder() {
        var sfuId = UUID.randomUUID();
        var sfuStreamId = UUID.randomUUID();
        var sfuPadId = UUID.randomUUID();
        var sfuTransportId = UUID.randomUUID();
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var streamDirection = this.randomGenerators.getRandomStreamDirection();
        var marker = this.randomGenerators.getRandomMarker();
        var sinkId = StreamDirection.OUTBOUND.equals(streamDirection) ? UUID.randomUUID() : null;
        var result = SfuRtpPadDTO.builder()
                .withServiceId(serviceId)
                .withMediaUnitId(mediaUnitId)
                .withSfuId(sfuId)
                .withSfuTransportId(sfuTransportId)
                .withStreamId(sfuStreamId)
                .withSinkId(sinkId)
                .withSfuRtpPadId(sfuPadId)
                .withStreamDirection(streamDirection)
                .withAddedTimestamp(timestamp)
                .withMarker(marker)
                ;
        return result;
    }

    public SfuStreamDTO getSfuStreamDTO() {
        var result = this.getSfuStreamDTOBuilder()
                .build();
        return result;
    }

    public SfuStreamDTO.Builder getSfuStreamDTOBuilder() {
        var sfuId = UUID.randomUUID();
        var sfuStreamId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var callId = UUID.randomUUID();
        var sfuTransportId = UUID.randomUUID();
        var result = SfuStreamDTO.builder()
                .withSfuId(sfuId)
                .withSfuTransportId(sfuTransportId)
                .withStreamId(sfuStreamId)
                .withTrackId(trackId)
                .withClientId(clientId)
                .withCallId(callId)
                ;
        return result;
    }

    public SfuSinkDTO getSfuSinkDTO() {
        var result = this.getSfuSinkDTOBuilder()
                .build();
        return result;
    }

    public SfuSinkDTO.Builder getSfuSinkDTOBuilder() {
        var sfuId = UUID.randomUUID();
        var sfuStreamId = UUID.randomUUID();
        var sfuSinkId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var callId = UUID.randomUUID();
        var sfuTransportId = UUID.randomUUID();
        var result = SfuSinkDTO.builder()
                .withSfuId(sfuId)
                .withSfuTransportId(sfuTransportId)
                .withStreamId(sfuStreamId)
                .withSinkId(sfuSinkId)
                .withTrackId(trackId)
                .withClientId(clientId)
                .withCallId(callId)
                ;
        return result;
    }
}
