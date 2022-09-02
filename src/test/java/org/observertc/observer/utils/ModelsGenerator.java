package org.observertc.observer.utils;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.dto.*;
import org.observertc.schemas.dtos.Models;

import java.util.UUID;

@Prototype
public class ModelsGenerator {

    RandomGenerators randomGenerators = new RandomGenerators();


    public ModelsGenerator() {

    }

    public Models.Call getCallDTO() {
        var result = this.getCallDTOBuilder()
                .build();
        return result;
    }

    public Models.Call.Builder getCallDTOBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID().toString();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var marker = this.randomGenerators.getRandomMarker();
        var result = Models.Call.newBuilder()
                .setServiceId(serviceId)
                .setRoomId(roomId)
                .setCallId(callId)
                .setStarted(timestamp)
                .setMarker(marker)
                ;
        return result;
    }

    public Models.Client getClientDTO() {
        var result = this.getClientDTOBuilder()
                .build();
        return result;
    }

    public Models.Client.Builder getClientDTOFromCallDTO(Models.Call callDTO) {
        var result = this.getClientDTOBuilderFromCallDTO(callDTO)
                .build()
                ;
        return result;
    }

    public Models.Client.Builder getClientDTOBuilderFromCallDTO(Models.Call callDTO) {
        var result = this.getClientDTOBuilder()
                .setCallId(callDTO.callId)
                .setServiceId(callDTO.serviceId)
                .setRoomId(callDTO.roomId)
                ;
        return result;
    }

    public Models.Client.Builder getClientDTOBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID();
        var userId = this.randomGenerators.getRandomTestUserIds();
        var timeZoneId = this.randomGenerators.getRandomTimeZoneId();
        var marker = this.randomGenerators.getRandomMarker();
        var result = Models.Client.newBuilder()
                .setServiceId(serviceId)
                .setRoomId(roomId)
                .setCallId(callId)
                .setMediaUnitId(mediaUnitId)
                .setClientId(clientId)
                .setUserId(userId)
                .setJoinedTimestamp(timestamp)
                .setTimeZoneId(timeZoneId)
                .setMarker(marker)
                ;
        return result;
    }

    public Models.PeerConnection getPeerConnectionDTO() {
        var result = this.getPeerConnectionDTOBuilder()
                .build()
                ;
        return result;
    }

    public Models.PeerConnection getPeerConnectionDTOFromClientDTO(ClientDTO clientDTO) {
        var result = this.getPeerConnectionDTOBuilderFromClientDTO(clientDTO)
                .build()
                ;
        return result;
    }

    public Models.PeerConnection.Builder getPeerConnectionDTOBuilderFromClientDTO(ClientDTO clientDTO) {
        var result = this.getPeerConnectionDTOBuilder()
                .setCallId(clientDTO.callId)
                .setServiceId(clientDTO.serviceId)
                .setRoomId(clientDTO.roomId)
                .setMediaUnitId(clientDTO.mediaUnitId)
                .setClientId(clientDTO.clientId)
                .setUserId(clientDTO.userId)
                ;
        return result;
    }

    public Models.PeerConnection.Builder getPeerConnectionDTOBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        var userId = this.randomGenerators.getRandomTestUserIds();
        var marker = this.randomGenerators.getRandomMarker();
        var result = Models.PeerConnection.newBuilder()
                .setServiceId(serviceId)
                .setRoomId(roomId)
                .setCallId(callId)
                .setMediaUnitId(mediaUnitId)
                .setClientId(clientId)
                .setPeerConnectionId(peerConnectionId)
                .setUserId(userId)
                .setCreatedTimestamp(timestamp)
                .setMarker(marker)
                ;
        return result;
    }

    public Models.InboundTrack getInboundTrackModel() {
        var result = this.getMediaTrackDTOBuilder().build();
        return result;
    }

    public Models.InboundTrack getInboundTrackFromPeerConnectionModel(Models.PeerConnection peerConnectionDTO) {
        var result = this.getInboundTrackBuilderFromPeerConnectionModel(peerConnectionDTO)
                .build()
                ;
        return result;
    }

    public Models.InboundTrack.Builder getInboundTrackBuilderFromPeerConnectionModel(Models.PeerConnection peerConnectionDTO) {
        var result = this.getInboundTrackModelBuilder()
                .setCallId(peerConnectionDTO.callId)
                .setServiceId(peerConnectionDTO.serviceId)
                .setRoomId(peerConnectionDTO.roomId)
                .setMediaUnitId(peerConnectionDTO.mediaUnitId)
                .setClientId(peerConnectionDTO.clientId)
                .setUserId(peerConnectionDTO.userId)
                .setPeerConnectionId(peerConnectionDTO.peerConnectionId)
                ;
        return result;
    }

    public Models.InboundTrack.Builder getInboundTrackModelBuilder() {
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
                .setServiceId(serviceId)
                .setRoomId(roomId)
                .setCallId(callId)
                .setMediaUnitId(mediaUnitId)
                .setClientId(clientId)
                .setPeerConnectionId(peerConnectionId)
                .setTrackId(trackId)
                .setUserId(userId)
                .setAddedTimestamp(timestamp)
                .setSSRC(ssrc)
                .setSfuStreamId(sfuStreamId)
                .setDirection(direction)
                .setMarker(marker)
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
                .setSfuId(sfuId)
                .setServiceId(serviceId)
                .setMediaUnitId(mediaUnitId)
                .setConnectedTimestamp(timestamp)
                .setTimeZoneId(timeZoneId)
                .setMarker(marker)
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
                .setSfuId(sfuDTO.sfuId)
                .setServiceId(sfuDTO.serviceId)
                .setMediaUnitId(sfuDTO.mediaUnitId)
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
                .setSfuId(sfuId)
                .setServiceId(serviceId)
                .setInternal(internal)
                .setTransportId(transportId)
                .setMediaUnitId(mediaUnitId)
                .setOpenedTimestamp(timestamp)
                .setMarker(marker)
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
                .setSfuId(sfuTransportDTO.sfuId)
                .setServiceId(sfuTransportDTO.serviceId)
                .setMediaUnitId(sfuTransportDTO.mediaUnitId)
                .setSfuTransportId(sfuTransportDTO.transportId)
                .setInternal(sfuTransportDTO.internal)
                ;
        return result;
    }

    public SfuRtpPadDTO.Builder getSfuRtpPadDTOBuilder() {
        var SSRC = UUID.randomUUID().getLeastSignificantBits();
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
                .setServiceId(serviceId)
                .setMediaUnitId(mediaUnitId)
                .setSfuId(sfuId)
                .setSfuTransportId(sfuTransportId)
                .setStreamId(sfuStreamId)
                .setSinkId(sinkId)
                .setSfuRtpPadId(sfuPadId)
                .setStreamDirection(streamDirection)
                .setAddedTimestamp(timestamp)
                .setMarker(marker)
                .setSsrc(SSRC)
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
                .setSfuId(sfuId)
                .setSfuTransportId(sfuTransportId)
                .setStreamId(sfuStreamId)
                .setTrackId(trackId)
                .setClientId(clientId)
                .setCallId(callId)
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
                .setSfuId(sfuId)
                .setSfuTransportId(sfuTransportId)
                .setStreamId(sfuStreamId)
                .setSinkId(sfuSinkId)
                .setTrackId(trackId)
                .setClientId(clientId)
                .setCallId(callId)
                ;
        return result;
    }
}
