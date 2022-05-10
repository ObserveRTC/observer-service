package org.observertc.observer.repositories;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class SfuRtpPadToMediaTrackBinder {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadToMediaTrackBinder.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {

    }

    @PreDestroy
    void teardown() {

    }

    public void onSfuRtpPadsAdded(List<SfuRtpPadDTO> sfuRtpPads) {
        var streamIds = sfuRtpPads.stream().map(pad -> pad.streamId).filter(Objects::nonNull).collect(Collectors.toSet());
        var sinkIds = sfuRtpPads.stream().map(pad -> pad.sinkId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, SfuStreamDTO> streamDTOs = 0 < streamIds.size() ? this.hazelcastMaps.getSfuStreams().getAll(streamIds) : Collections.EMPTY_MAP;
        Map<UUID, SfuSinkDTO> sinkDTOs = 0 < sinkIds.size() ? this.hazelcastMaps.getSfuSinks().getAll(sinkIds) : Collections.EMPTY_MAP;
        sfuRtpPads.forEach(sfuRtpPad -> {
            switch (sfuRtpPad.streamDirection) {
                case INBOUND:
                    this.bindSfuInboundRtpPad(streamDTOs, sfuRtpPad);
                    return;
                case OUTBOUND:
                    this.bindSfuOutboundRtpPad(sinkDTOs, sfuRtpPad);
                    return;
            }
        });
    }

    public void onMediaTracksAdded(List<MediaTrackDTO> mediaTrackDTOS) {
        var streamIds = mediaTrackDTOS.stream().map(mediaTrackDTO -> mediaTrackDTO.sfuStreamId).filter(Objects::nonNull).collect(Collectors.toSet());
        var sinkIds = mediaTrackDTOS.stream().map(mediaTrackDTO -> mediaTrackDTO.sfuSinkId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, SfuStreamDTO> streamDTOs = 0 < streamIds.size() ? this.hazelcastMaps.getSfuStreams().getAll(streamIds) : Collections.EMPTY_MAP;
        Map<UUID, SfuSinkDTO> sinkDTOs = 0 < sinkIds.size() ? this.hazelcastMaps.getSfuSinks().getAll(sinkIds) : Collections.EMPTY_MAP;
        mediaTrackDTOS.forEach(mediaTrackDTO -> {
            switch (mediaTrackDTO.direction) {
                case INBOUND:
                    this.bindInboundMediaTrack(sinkDTOs, mediaTrackDTO);
                    return;
                case OUTBOUND:
                    this.bindOutboundMediaTrack(streamDTOs, mediaTrackDTO);
                    return;
            }
        });
    }

    private void bindOutboundMediaTrack(Map<UUID, SfuStreamDTO> streamDTOs, MediaTrackDTO mediaTrackDTO) {
        if (Objects.isNull(mediaTrackDTO.sfuStreamId)) {
            return;
        }
        var sfuStream = streamDTOs.get(mediaTrackDTO.sfuStreamId);
        if (Objects.isNull(sfuStream)) {
            sfuStream = SfuStreamDTO.builder()
                    .withStreamId(mediaTrackDTO.sfuStreamId)
                    .withTrackId(mediaTrackDTO.trackId)
                    .withPeerConnectionId(mediaTrackDTO.peerConnectionId)
                    .withClientId(mediaTrackDTO.clientId)
                    .withCallId(mediaTrackDTO.callId)
                    .build();
            this.hazelcastMaps.getSfuStreams().put(sfuStream.sfuStreamId, sfuStream);
            return;
        }
        boolean incompleteByClientSide = Objects.isNull(sfuStream.peerConnectionId);
        if (!incompleteByClientSide) {
            return;
        }
        sfuStream = SfuStreamDTO.builder().from(sfuStream)
                .withTrackId(mediaTrackDTO.trackId)
                .withPeerConnectionId(mediaTrackDTO.peerConnectionId)
                .withClientId(mediaTrackDTO.clientId)
                .withCallId(mediaTrackDTO.callId)
                .build();
        this.hazelcastMaps.getSfuStreams().put(sfuStream.sfuStreamId, sfuStream);
        logger.debug("Sfu Stream is completed by Client Outbound Media Track update. {}", JsonUtils.objectToString(sfuStream));
    }

    private void bindInboundMediaTrack(Map<UUID, SfuSinkDTO> sinkDTOs, MediaTrackDTO mediaTrackDTO) {
        if (Objects.isNull(mediaTrackDTO.sfuSinkId)) {
            return;
        }
        var sfuSink = sinkDTOs.get(mediaTrackDTO.sfuSinkId);
        if (Objects.isNull(sfuSink)) {
            sfuSink = SfuSinkDTO.builder()
                    .withSinkId(mediaTrackDTO.sfuSinkId)
                    .withStreamId(mediaTrackDTO.sfuStreamId)
                    .withTrackId(mediaTrackDTO.trackId)
                    .withPeerConnectionId(mediaTrackDTO.peerConnectionId)
                    .withClientId(mediaTrackDTO.clientId)
                    .withCallId(mediaTrackDTO.callId)
                    .build();
            this.hazelcastMaps.getSfuSinks().put(sfuSink.sfuSinkId, sfuSink);
            return;
        }
        boolean incompleteByClientSide = Objects.isNull(sfuSink.peerConnectionId);
        if (!incompleteByClientSide) {
            return;
        }
        sfuSink = SfuSinkDTO.builder().from(sfuSink)
                .withTrackId(mediaTrackDTO.trackId)
                .withPeerConnectionId(mediaTrackDTO.peerConnectionId)
                .withClientId(mediaTrackDTO.clientId)
                .withCallId(mediaTrackDTO.callId)
                .build();
        this.hazelcastMaps.getSfuSinks().put(sfuSink.sfuSinkId, sfuSink);
        logger.debug("Sfu Stream is completed by Client Outbound Media Track update. {}", JsonUtils.objectToString(sfuSink));
    }

    private void bindSfuInboundRtpPad(Map<UUID, SfuStreamDTO> streamDTOs, SfuRtpPadDTO inboundSfuRtpPad) {
        if (inboundSfuRtpPad.internal || Objects.isNull(inboundSfuRtpPad.streamId)) {
            return;
        }
        var sfuStream = streamDTOs.get(inboundSfuRtpPad.streamId);
        if (Objects.isNull(sfuStream)) {
            sfuStream = SfuStreamDTO.builder()
                    .withStreamId(inboundSfuRtpPad.streamId)
                    .withSfuId(inboundSfuRtpPad.sfuId)
                    .withSfuTransportId(inboundSfuRtpPad.transportId)
                    .build();
            this.hazelcastMaps.getSfuStreams().put(sfuStream.sfuStreamId, sfuStream);
            this.hazelcastMaps.getSfuStreamIdToRtpPadIds().put(sfuStream.sfuStreamId, inboundSfuRtpPad.rtpPadId);
            return;
        }
        boolean incompleteBySfuSide = Objects.isNull(sfuStream.sfuTransportId);
        if (!incompleteBySfuSide) {
            return;
        }
        sfuStream = SfuStreamDTO.builder().from(sfuStream)
                .withSfuId(inboundSfuRtpPad.sfuId)
                .withSfuTransportId(inboundSfuRtpPad.transportId)
                .build();
        this.hazelcastMaps.getSfuStreams().put(sfuStream.sfuStreamId, sfuStream);
        this.hazelcastMaps.getSfuStreamIdToRtpPadIds().put(sfuStream.sfuStreamId, inboundSfuRtpPad.rtpPadId);
        logger.debug("Sfu Stream is completed by SfuRtpPad update. {}", JsonUtils.objectToString(sfuStream));
    }



    private void bindSfuOutboundRtpPad(Map<UUID, SfuSinkDTO> sinkDTOs, SfuRtpPadDTO outboundSfuRtpPad) {
        if (Objects.isNull(outboundSfuRtpPad.sinkId)) {
            return;
        }
        if (outboundSfuRtpPad.internal) {
//            if (outboundSfuRtpPad.streamId != null) {
//                this.hazelcastMaps.getSfuStreamIdToInternalSfuSinkIds().put(outboundSfuRtpPad.streamId, outboundSfuRtpPad.rtpPadId);
//            }
            return;
        }
        var sfuSink = sinkDTOs.get(outboundSfuRtpPad.sinkId);
        if (Objects.isNull(sfuSink)) {
            sfuSink = SfuSinkDTO.builder()
                    .withSfuId(outboundSfuRtpPad.sfuId)
                    .withSfuTransportId(outboundSfuRtpPad.transportId)
                    .withSinkId(outboundSfuRtpPad.sinkId)
                    .withStreamId(outboundSfuRtpPad.streamId)
                    .build();
            this.hazelcastMaps.getSfuSinks().put(sfuSink.sfuSinkId, sfuSink);
            return;
        }
        boolean incompleteBySfuSide = Objects.isNull(sfuSink.sfuTransportId);
        if (!incompleteBySfuSide) {
            return;
        }
        sfuSink = SfuSinkDTO.builder().from(sfuSink)
                .withSfuId(outboundSfuRtpPad.sfuId)
                .withSfuTransportId(outboundSfuRtpPad.transportId)
                .withStreamId(outboundSfuRtpPad.streamId)
                .build();
        this.hazelcastMaps.getSfuSinks().put(sfuSink.sfuSinkId, sfuSink);
        this.hazelcastMaps.getSfuSinkIdToRtpPadIds().put(sfuSink.sfuSinkId, outboundSfuRtpPad.rtpPadId);
        logger.debug("Sfu Sink is completed by SfuRtpPad update. {}", JsonUtils.objectToString(sfuSink));
    }
}
