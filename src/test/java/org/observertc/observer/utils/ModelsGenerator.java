package org.observertc.observer.utils;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configs.MediaKind;
import org.observertc.schemas.dtos.Models;

import java.util.UUID;

@Prototype
public class ModelsGenerator {

    RandomGenerators randomGenerators = new RandomGenerators();


    public ModelsGenerator() {

    }

    public Models.Call getCallModel() {
        var result = this.getCallModelBuilder()
                .build();
        return result;
    }

    public Models.Call.Builder getCallModelBuilder() {
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
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }

    public Models.Client getClientModel() {
        var result = this.getClientModelBuilder()
                .build();
        return result;
    }

    public Models.Client getClientModelFromCallDTO(Models.Call callDTO) {
        var result = this.getClientModelBuilderFromCallModel(callDTO)
                .build()
                ;
        return result;
    }

    public Models.Client.Builder getClientModelBuilderFromCallModel(Models.Call callDTO) {
        var result = this.getClientModelBuilder()
                .setCallId(callDTO.getCallId())
                .setServiceId(callDTO.getServiceId())
                .setRoomId(callDTO.getRoomId())
                ;
        return result;
    }

    public Models.Client.Builder getClientModelBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID().toString();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID().toString();
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
                .setJoined(timestamp)
                .setTimeZoneId(timeZoneId)
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }

    public Models.PeerConnection getPeerConnectionModel() {
        var result = this.getPeerConnectionModelBuilder()
                .build()
                ;
        return result;
    }

    public Models.PeerConnection getPeerConnectionModelFromClientModel(Models.Client clientDTO) {
        var result = this.getPeerConnectionModelBuilderFromClientModel(clientDTO)
                .build()
                ;
        return result;
    }

    public Models.PeerConnection.Builder getPeerConnectionModelBuilderFromClientModel(Models.Client clientDTO) {
        var result = this.getPeerConnectionModelBuilder()
                .setCallId(clientDTO.getCallId())
                .setServiceId(clientDTO.getServiceId())
                .setRoomId(clientDTO.getRoomId())
                .setMediaUnitId(clientDTO.getMediaUnitId())
                .setClientId(clientDTO.getClientId())
                .setUserId(clientDTO.getUserId())
                ;
        return result;
    }

    public Models.PeerConnection.Builder getPeerConnectionModelBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID().toString();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID().toString();
        var peerConnectionId = UUID.randomUUID().toString();
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
                .setOpened(timestamp)
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }

    public Models.InboundTrack getInboundTrackModel() {
        var result = this.getInboundTrackModelBuilder().build();
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
                .setCallId(peerConnectionDTO.getCallId())
                .setServiceId(peerConnectionDTO.getServiceId())
                .setRoomId(peerConnectionDTO.getRoomId())
                .setMediaUnitId(peerConnectionDTO.getMediaUnitId())
                .setClientId(peerConnectionDTO.getClientId())
                .setUserId(peerConnectionDTO.getUserId())
                .setPeerConnectionId(peerConnectionDTO.getPeerConnectionId())
                ;
        return result;
    }

    public Models.InboundTrack.Builder getInboundTrackModelBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID().toString();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID().toString();
        var peerConnectionId = UUID.randomUUID().toString();
        var trackId = UUID.randomUUID().toString();
        var userId = this.randomGenerators.getRandomTestUserIds();
        var ssrc = this.randomGenerators.getRandomSSRC();
        var sfuStreamId = UUID.fromString(callId).getLeastSignificantBits() % 2L == 0 ? UUID.randomUUID().toString() : null;
        var sfuSinkId = UUID.fromString(callId).getLeastSignificantBits() % 2L == 0 ? UUID.randomUUID().toString() : null;
        var marker = this.randomGenerators.getRandomMarker();
        var kind = UUID.fromString(callId).getMostSignificantBits() % 2L == 0 ? MediaKind.AUDIO : MediaKind.VIDEO;
        var result = Models.InboundTrack.newBuilder()
                .setServiceId(serviceId)
                .setRoomId(roomId)
                .setCallId(callId)
                .setMediaUnitId(mediaUnitId)
                .setClientId(clientId)
                .setPeerConnectionId(peerConnectionId)
                .setTrackId(trackId)
                .setUserId(userId)
                .setAdded(timestamp)
                .addSsrc(ssrc)
                .setKind(kind.name())
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        if (sfuStreamId != null) {
            result.setSfuStreamId(sfuStreamId);
        }
        if (sfuSinkId != null) {
            result.setSfuSinkId(sfuSinkId);
        }
        return result;
    }



    public Models.OutboundTrack getOutboundTrackModel() {
        var result = this.getOutboundTrackModelBuilder().build();
        return result;
    }

    public Models.OutboundTrack getOutboundTrackFromPeerConnectionModel(Models.PeerConnection peerConnectionDTO) {
        var result = this.getOutboundTrackBuilderFromPeerConnectionModel(peerConnectionDTO)
                .build()
                ;
        return result;
    }

    public Models.OutboundTrack.Builder getOutboundTrackBuilderFromPeerConnectionModel(Models.PeerConnection peerConnectionDTO) {
        var result = this.getOutboundTrackModelBuilder()
                .setCallId(peerConnectionDTO.getCallId())
                .setServiceId(peerConnectionDTO.getServiceId())
                .setRoomId(peerConnectionDTO.getRoomId())
                .setMediaUnitId(peerConnectionDTO.getMediaUnitId())
                .setClientId(peerConnectionDTO.getClientId())
                .setUserId(peerConnectionDTO.getUserId())
                .setPeerConnectionId(peerConnectionDTO.getPeerConnectionId())
                ;
        return result;
    }

    public Models.OutboundTrack.Builder getOutboundTrackModelBuilder() {
        var serviceId = this.randomGenerators.getRandomServiceId();
        var roomId = this.randomGenerators.getRandomTestRoomIds();
        var callId = UUID.randomUUID().toString();
        var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var clientId = UUID.randomUUID().toString();
        var peerConnectionId = UUID.randomUUID().toString();
        var trackId = UUID.randomUUID().toString();
        var userId = this.randomGenerators.getRandomTestUserIds();
        var ssrc = this.randomGenerators.getRandomSSRC();
        var sfuStreamId = UUID.fromString(callId).getLeastSignificantBits() % 2L == 0 ? UUID.randomUUID().toString() : null;
        var marker = this.randomGenerators.getRandomMarker();
        var kind = UUID.fromString(callId).getMostSignificantBits() % 2L == 0 ? MediaKind.AUDIO : MediaKind.VIDEO;
        var result = Models.OutboundTrack.newBuilder()
                .setServiceId(serviceId)
                .setRoomId(roomId)
                .setCallId(callId)
                .setMediaUnitId(mediaUnitId)
                .setClientId(clientId)
                .setPeerConnectionId(peerConnectionId)
                .setTrackId(trackId)
                .setUserId(userId)
                .setAdded(timestamp)
                .addSsrc(ssrc)
                .setKind(kind.name())
//                .setSfuStreamId(sfuStreamId)
//                .setMarker(marker)
                ;
        if (sfuStreamId != null) {
            result.setSfuStreamId(sfuStreamId);
        }
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }


    public Models.Sfu getSfuModel() {
        var result = this.getSfuModelBuilder().build();
        return result;
    }



    public Models.Sfu.Builder getSfuModelBuilder() {
        var sfuId = UUID.randomUUID().toString();
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var timeZoneId = this.randomGenerators.getRandomTimeZoneId();
        var marker = this.randomGenerators.getRandomMarker();
        var result = Models.Sfu.newBuilder()
                .setSfuId(sfuId)
                .setServiceId(serviceId)
                .setMediaUnitId(mediaUnitId)
                .setJoined(timestamp)
                .setTimeZoneId(timeZoneId)
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }

    public Models.SfuTransport getSfuTransportDTOFromSfuDTO(Models.Sfu sfuDTO) {
        var result = this.getSfuTransportModelBuilderFromSfuModel(sfuDTO)
                .build()
                ;
        return result;
    }

    public Models.SfuTransport.Builder getSfuTransportModelBuilderFromSfuModel(Models.Sfu sfuDTO) {
        var result = this.getSfuTransportModelBuilder()
                .setSfuId(sfuDTO.getSfuId())
                .setServiceId(sfuDTO.getServiceId())
                .setMediaUnitId(sfuDTO.getMediaUnitId())
                ;
        return result;
    }

    public Models.SfuTransport getSfuTransportModel() {
        var result = this.getSfuTransportModelBuilder()
                .build();
        return result;
    }

    public Models.SfuTransport.Builder getSfuTransportModelBuilder() {
        var sfuId = UUID.randomUUID().toString();
        var transportId = UUID.randomUUID().toString();
        var internal = this.randomGenerators.getRandomPort() % 2 == 0;
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var marker = this.randomGenerators.getRandomMarker();
        var result = Models.SfuTransport.newBuilder()
                .setSfuId(sfuId)
                .setServiceId(serviceId)
                .setInternal(internal)
                .setTransportId(transportId)
                .setMediaUnitId(mediaUnitId)
                .setOpened(timestamp)
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }

    public Models.SfuInboundRtpPad getSfuInboundRtpPad() {
        var result = this.getSfuInboundRtpPadModelBuilder()
                .build()
                ;
        return result;
    }

    public Models.SfuInboundRtpPad getSfuInboundRtpPadModelFromSfuTransportModel(Models.SfuTransport sfuTransportDTO) {
        var result = this.getSfuInboundRtpPadBuilderFromSfuTransportModel(sfuTransportDTO)
                .build()
                ;
        return result;
    }

    public Models.SfuInboundRtpPad.Builder getSfuInboundRtpPadBuilderFromSfuTransportModel(Models.SfuTransport sfuTransportDTO) {
        var result = this.getSfuInboundRtpPadModelBuilder()
                .setSfuId(sfuTransportDTO.getSfuId())
                .setServiceId(sfuTransportDTO.getServiceId())
                .setMediaUnitId(sfuTransportDTO.getMediaUnitId())
                .setSfuTransportId(sfuTransportDTO.getTransportId())
                .setInternal(sfuTransportDTO.getInternal())
                ;
        return result;
    }

    public Models.SfuOutboundRtpPad getSfuOutboundRtpPad() {
        var result = this.getSfuOutboundRtpPadModelBuilder()
                .build()
                ;
        return result;
    }

    public Models.SfuOutboundRtpPad getSfuOutboundRtpPadModelFromSfuTransportModel(Models.SfuTransport sfuTransportDTO) {
        var result = this.getSfuOutboundRtpPadBuilderFromSfuTransportModel(sfuTransportDTO)
                .build()
                ;
        return result;
    }

    public Models.SfuOutboundRtpPad.Builder getSfuOutboundRtpPadBuilderFromSfuTransportModel(Models.SfuTransport sfuTransportDTO) {
        var result = this.getSfuOutboundRtpPadModelBuilder()
                .setSfuId(sfuTransportDTO.getSfuId())
                .setServiceId(sfuTransportDTO.getServiceId())
                .setMediaUnitId(sfuTransportDTO.getMediaUnitId())
                .setSfuTransportId(sfuTransportDTO.getTransportId())
                .setInternal(sfuTransportDTO.getInternal())
                ;
        return result;
    }


    public Models.SfuInboundRtpPad.Builder getSfuInboundRtpPadModelBuilder() {
        var SSRC = UUID.randomUUID().getLeastSignificantBits();
        var sfuId = UUID.randomUUID().toString();
        var sfuStreamId = UUID.randomUUID().toString();
        var sfuPadId = UUID.randomUUID().toString();
        var sfuTransportId = UUID.randomUUID().toString();
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var marker = this.randomGenerators.getRandomMarker();
        var result = Models.SfuInboundRtpPad.newBuilder()
                .setServiceId(serviceId)
                .setMediaUnitId(mediaUnitId)
                .setSfuId(sfuId)
                .setSfuTransportId(sfuTransportId)
                .setSfuStreamId(sfuStreamId)
                .setRtpPadId(sfuPadId)
                .setAdded(timestamp)
                .setInternal(false)
                .setSsrc(SSRC)
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }

    public Models.SfuOutboundRtpPad.Builder getSfuOutboundRtpPadModelBuilder() {
        var SSRC = UUID.randomUUID().getLeastSignificantBits();
        var sfuId = UUID.randomUUID().toString();
        var sfuStreamId = UUID.randomUUID().toString();
        var sfuSinkId = UUID.randomUUID().toString();
        var sfuPadId = UUID.randomUUID().toString();
        var sfuTransportId = UUID.randomUUID().toString();
        var serviceId = this.randomGenerators.getRandomServiceId();
        var mediaUnitId = this.randomGenerators.getRandomSFUSideMediaUnitId();
        var timestamp = this.randomGenerators.getRandomTimestamp();
        var marker = this.randomGenerators.getRandomMarker();
        var result = Models.SfuOutboundRtpPad.newBuilder()
                .setServiceId(serviceId)
                .setMediaUnitId(mediaUnitId)
                .setSfuId(sfuId)
                .setSfuTransportId(sfuTransportId)
//                .setSfuStreamId(sfuStreamId)
//                .setSfuSinkId(sfuSinkId)
                .setRtpPadId(sfuPadId)
                .setAdded(timestamp)
                .setInternal(false)
                .setSsrc(SSRC)
                ;
        if (marker != null) {
            result.setMarker(marker);
        }
        if (sfuStreamId != null) {
            result.setSfuStreamId(sfuStreamId);
        }
        if (sfuSinkId != null) {
            result.setSfuSinkId(sfuSinkId);
        }
        if (marker != null) {
            result.setMarker(marker);
        }
        return result;
    }
}
