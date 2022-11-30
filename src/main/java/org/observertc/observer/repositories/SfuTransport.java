package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SfuTransport {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransport.class);

    private final AtomicReference<Models.SfuTransport> modelHolder;
    private final SfusRepository sfusRepository;
    private final SfuTransportsRepository sfuTransportsRepository;
    private final SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;
    private final SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;
    private final SfuSctpChannelsRepository sfuSctpStreamsRepository;

    SfuTransport(
            Models.SfuTransport model,
            SfusRepository sfusRepository,
            SfuTransportsRepository sfuTransportsRepository,
            SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository,
            SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository,
            SfuSctpChannelsRepository sfuSctpStreamsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfusRepository = sfusRepository;
        this.sfuTransportsRepository = sfuTransportsRepository;
        this.sfuInboundRtpPadsRepository = sfuInboundRtpPadsRepository;
        this.sfuOutboundRtpPadsRepository = sfuOutboundRtpPadsRepository;
        this.sfuSctpStreamsRepository = sfuSctpStreamsRepository;
    }

    public String getServiceId() {
        var model = this.modelHolder.get();
        return model.getServiceId();
    }

    public Sfu getSfu() {
        var model = this.modelHolder.get();
        return this.sfusRepository.get(model.getSfuId());
    }

    public String getSfuId() {
        var model = this.modelHolder.get();
        return model.getSfuId();
    }

    public String getSfuTransportId() {
        var model = this.modelHolder.get();
        return model.getTransportId();
    }

    public boolean isInternal() {
        var model = this.modelHolder.get();
        return model.getInternal();
    }

    public Long getOpened() {
        var model = this.modelHolder.get();
        return model.getOpened();
    }

    public Long getTouched() {
        var model = modelHolder.get();
        if (!model.hasTouched()) {
            return null;
        }
        return model.getTouched();
    }

    public void touch(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.SfuTransport.newBuilder(model)
                .setTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public String getMediaUnitId() {
        var model = this.modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

//    public Collection<String> getStreamIds() {
//        var model = this.modelHolder.get();
//        if (model.getInboundRtpPadIdsCount() < 1) {
//            return Collections.emptyList();
//        }
//        return model.getInboundRtpPadIdsList();
//    }

    public Collection<String> getInboundRtpPadIds() {
        var model = this.modelHolder.get();
        if (model.getInboundRtpPadIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getInboundRtpPadIdsList();
    }

    public boolean hasInboundRtpPad(String rtpPadId) {
        var model = this.modelHolder.get();
        if (model.getInboundRtpPadIdsCount() < 1) {
            return false;
        }
        return model.getInboundRtpPadIdsList().contains(rtpPadId);
    }

    public SfuInboundRtpPad getInboundRtpPad(String rtpPadId) {
        var model = this.modelHolder.get();
        if (model.getInboundRtpPadIdsCount() < 1) {
            return null;
        }
        return this.sfuInboundRtpPadsRepository.get(rtpPadId);
    }

    public Map<String, SfuInboundRtpPad> getInboundRtpPads() {
        var model = this.modelHolder.get();
        if (model.getInboundRtpPadIdsCount() < 1) {
            return Collections.emptyMap();
        }
        var rtpPadIds = this.getInboundRtpPadIds();
        return this.sfuInboundRtpPadsRepository.getAll(rtpPadIds);
    }

    public SfuInboundRtpPad addInboundRtpPad(String rtpPadId, Long ssrc, String sfuStreamId, Long timestamp, String marker) throws AlreadyCreatedException {
        var model = modelHolder.get();
        if (0 < model.getInboundRtpPadIdsCount()) {
            var inboundRtpPadIds = model.getInboundRtpPadIdsList();
            if (inboundRtpPadIds.contains(rtpPadId)) {
                throw AlreadyCreatedException.wrapSfuInboundRtpPad(rtpPadId);
            }
        }

        var inboundRtpPadModelBuilder = Models.SfuInboundRtpPad.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuId(model.getSfuId())
                .setSfuTransportId(model.getTransportId())
                // sfuStreamId
                .setRtpPadId(rtpPadId)
                .setSsrc(ssrc)
                .setInternal(model.getInternal())
                .setAdded(timestamp)
                .setTouched(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                ;
        if (sfuStreamId != null) {
            inboundRtpPadModelBuilder.setSfuStreamId(sfuStreamId);
        }
        if (marker != null) {
            inboundRtpPadModelBuilder.setMarker(marker);
        }
        var inboundRtpPadModel = inboundRtpPadModelBuilder.build();
        var newModel = Models.SfuTransport.newBuilder(model)
                .addInboundRtpPadIds(rtpPadId)
                .build();

        this.updateModel(newModel);
        this.sfuInboundRtpPadsRepository.update(inboundRtpPadModel);
        return this.sfuInboundRtpPadsRepository.wrapInboundRtpPad(inboundRtpPadModel);

    }

    public boolean removeInboundRtpPad(String rtpPadId) {
        var model = modelHolder.get();
        if (model.getInboundRtpPadIdsCount() < 1) {
            return false;
        }
        var inboundRtpPadIds = model.getInboundRtpPadIdsList();
        if (!inboundRtpPadIds.contains(rtpPadId)) {
            return false;
        }
        var newInboundRtpPadIds = inboundRtpPadIds.stream().filter(savedValue -> savedValue != rtpPadId)
                .collect(Collectors.toSet());

        var newModel = Models.SfuTransport.newBuilder(model)
                .clearInboundRtpPadIds()
                .addAllInboundRtpPadIds(newInboundRtpPadIds)
                .build();

        this.updateModel(newModel);
        this.sfuInboundRtpPadsRepository.delete(rtpPadId);
        return true;
    }


    public Collection<String> getOutboundRtpPadIds() {
        var model = this.modelHolder.get();
        if (model.getOutboundRtpPadIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getOutboundRtpPadIdsList();
    }

    public SfuOutboundRtpPad getOutboundRtpPad(String rtpPadId) {
        var model = this.modelHolder.get();
        if (model.getOutboundRtpPadIdsCount() < 1) {
            return null;
        }
        return this.sfuOutboundRtpPadsRepository.get(rtpPadId);
    }

    public Map<String, SfuOutboundRtpPad> getOutboundRtpPads() {
        var model = this.modelHolder.get();
        if (model.getOutboundRtpPadIdsCount() < 1) {
            return Collections.emptyMap();
        }
        var rtpPadIds = this.getOutboundRtpPadIds();
        return this.sfuOutboundRtpPadsRepository.getAll(rtpPadIds);
    }

    public SfuOutboundRtpPad addOutboundRtpPad(String rtpPadId, Long ssrc, String sfuStreamId, String sfuSinkId, Long timestamp, String marker) throws AlreadyCreatedException {
        var model = modelHolder.get();
        if (0 < model.getOutboundRtpPadIdsCount()) {
            var rtpPadIds = model.getOutboundRtpPadIdsList();
            if (rtpPadIds.contains(rtpPadId)) {
                throw AlreadyCreatedException.wrapSfuOutboundRtpPad(rtpPadId);
            }
        }

        var sfuOutboundRtpPadModelBuilder = Models.SfuOutboundRtpPad.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuId(model.getSfuId())
                .setSfuTransportId(model.getTransportId())
                // sfuStreamId
                // sfuSinkId
                .setRtpPadId(rtpPadId)
                .setSsrc(ssrc)
                .setInternal(model.getInternal())
                .setAdded(timestamp)
                .setTouched(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                ;

        if (sfuStreamId != null) {
            sfuOutboundRtpPadModelBuilder.setSfuStreamId(sfuStreamId);
        }
        if (sfuSinkId != null) {
            sfuOutboundRtpPadModelBuilder.setSfuSinkId(sfuSinkId);
        }
        if (marker != null) {
            sfuOutboundRtpPadModelBuilder.setMarker(marker);
        }
        var sfuOutboundRtpPadModel = sfuOutboundRtpPadModelBuilder.build();
        var newModel = Models.SfuTransport.newBuilder(model)
                .addOutboundRtpPadIds(rtpPadId)
                .build();

        this.updateModel(newModel);
        this.sfuOutboundRtpPadsRepository.update(sfuOutboundRtpPadModel);
        var result = this.sfuOutboundRtpPadsRepository.wrapSfuOutboundRtpPad(sfuOutboundRtpPadModel);
        if (result != null && model.getInternal() && sfuStreamId != null && sfuSinkId != null) {
            // add MediaSink
            if (!result.createMediaSink()) {
                logger.warn("Cannot add internal media sink for media stream {}", sfuStreamId);
            }
        }
        return result;
    }

    public boolean removeOutboundRtpPad(String rtpPadId) {
        var model = modelHolder.get();
        if (model.getOutboundRtpPadIdsCount() < 1) {
            return false;
        }
        var rtpPadIds = model.getOutboundRtpPadIdsList();
        if (!rtpPadIds.contains(rtpPadId)) {
            return false;
        }
        var newOutboundRtpPadIds = rtpPadIds.stream().filter(actualPcId -> actualPcId != rtpPadId)
                .collect(Collectors.toSet());

        var newModel = Models.SfuTransport.newBuilder(model)
                .clearOutboundRtpPadIds()
                .addAllOutboundRtpPadIds(newOutboundRtpPadIds)
                .build();

        this.updateModel(newModel);
        this.sfuOutboundRtpPadsRepository.delete(rtpPadId);
        return true;
    }


    public Collection<String> getSctpChannelIds() {
        var model = this.modelHolder.get();
        if (model.getSctpChannelIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getSctpChannelIdsList();
    }

    public SfuSctpChannel getSctpChannel(String sctpStreamId) {
        var model = this.modelHolder.get();
        if (model.getSctpChannelIdsCount() < 1) {
            return null;
        }
        return this.sfuSctpStreamsRepository.get(sctpStreamId);
    }

    public Map<String, SfuSctpChannel> getSctpStreams() {
        var model = this.modelHolder.get();
        if (model.getSctpChannelIdsCount() < 1) {
            return Collections.emptyMap();
        }
        var sctpStreamIds = this.getSctpChannelIds();
        return this.sfuSctpStreamsRepository.getAll(sctpStreamIds);
    }

    public SfuSctpChannel addSctpChannel(String sctpStreamId, String sctpChannelId, Long timestamp, String marker) throws AlreadyCreatedException {
        var model = modelHolder.get();
        if (0 < model.getSctpChannelIdsCount()) {
            var sctpChannelIds = model.getSctpChannelIdsList();
            if (sctpChannelIds.contains(sctpChannelId)) {
                throw AlreadyCreatedException.wrapSfuSctpStream(sctpChannelId);
            }
        }

        var sctpChannelModelBuilder = Models.SfuSctpChannel.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuId(model.getSfuId())
                .setSfuTransportId(model.getTransportId())
                .setSfuSctpStreamId(sctpStreamId)
                .setSfuSctpChannelId(sctpChannelId)
                .setOpened(timestamp)
                .setTouched(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                ;

        if (marker != null) {
            sctpChannelModelBuilder.setMarker(marker);
        }
        var sctpStreamModel = sctpChannelModelBuilder.build();
        var newModel = Models.SfuTransport.newBuilder(model)
                .addSctpChannelIds(sctpChannelId)
                .build();

        this.updateModel(newModel);
        this.sfuSctpStreamsRepository.update(sctpStreamModel);
        return this.sfuSctpStreamsRepository.wrap(sctpStreamModel);
    }

    public boolean removeSctpStream(String sctpStreamId) {
        var model = modelHolder.get();
        if (model.getSctpChannelIdsCount() < 1) {
            return false;
        }
        var sctpStreamIds = model.getSctpChannelIdsList();
        if (!sctpStreamIds.contains(sctpStreamId)) {
            return false;
        }
        var newSctpStreamIds = sctpStreamIds.stream().filter(savedId -> savedId != sctpStreamId)
                .collect(Collectors.toSet());

        var newModel = Models.SfuTransport.newBuilder(model)
                .clearSctpChannelIds()
                .addAllOutboundRtpPadIds(newSctpStreamIds)
                .build();

        this.updateModel(newModel);
        this.sfuSctpStreamsRepository.delete(sctpStreamId);
        return true;
    }

    public Models.SfuTransport getModel() {
        return this.modelHolder.get();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.SfuTransport newModel) {
        this.modelHolder.set(newModel);
        this.sfuTransportsRepository.update(newModel);
    }

}
