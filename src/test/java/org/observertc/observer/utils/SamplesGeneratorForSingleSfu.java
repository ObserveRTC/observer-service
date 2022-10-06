package org.observertc.observer.utils;

import org.observertc.observer.common.MediaKind;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.SamplesVisitor;

import java.util.LinkedList;
import java.util.List;

public class SamplesGeneratorForSingleSfu {


    private final ModelsMapGenerator modelsMapGenerator;
    private final List<ClientSideSamplesGenerator> clientSideSamplesGenerators = new LinkedList<>();
    private final RandomGenerators randomGenerators;


    public SamplesGeneratorForSingleSfu() {
        this.modelsMapGenerator = new ModelsMapGenerator().generateSingleSfuCase();
        this.randomGenerators = new RandomGenerators();
        for (var clientModel : this.modelsMapGenerator.getClientModels().values()) {
            var clientSideSampleGenerator = this.createClientSideSamplesGenerator(clientModel.getClientId());
            this.clientSideSamplesGenerators.add(clientSideSampleGenerator);
        }
    }

    public ObservedClientSamples getObservedClientSamples() {
        var result = ObservedClientSamples.builder();
        for (var generator : this.clientSideSamplesGenerators) {
            var samples = generator.get();
            var serviceId = this.modelsMapGenerator.getCallModel().getServiceId();
            var mediaUnitId = this.randomGenerators.getRandomClientSideMediaUnitId();
            SamplesVisitor.streamClientSamples(samples)
                    .map(clientSample -> {
                        return ObservedClientSample.builder()
                                .setServiceId(serviceId)
                                .setMediaUnitId(mediaUnitId)
                                .setClientSample(clientSample)
                                .build();
                    }).forEach(result::addObservedClientSample);
        }
        return result.build();
    }

    public SamplesGeneratorForSingleSfu saveTo(HamokStorages hamokStorages) {
        this.modelsMapGenerator.saveTo(hamokStorages);
        return this;
    }

    private ClientSideSamplesGenerator createClientSideSamplesGenerator(String clientId) {
        var clientModel = this.modelsMapGenerator.getClientModels().get(clientId);
        var result = new ClientSideSamplesGenerator()
                .setClientId(clientModel.getClientId())
                .setCallId(clientModel.getCallId())
                .setMarker(clientModel.hasMarker() ? clientModel.getMarker() : null)
                .setRoomId(clientModel.getRoomId())
                .setUserId(clientModel.hasUserId() ? clientModel.getUserId() : null)
                .setTimeZoneOffsetInHours( -3)
                .addBrowser()
                .addCertificate()
                .addEngine()
                .addPlatform()
                .addExtensionStat()
                .addMediaConstraint("constraint");

        for (var peerConnectionModel : this.modelsMapGenerator.getPeerConnectionModels().values()) {
            if (peerConnectionModel.getClientId().equals(clientId)) {
                result.addPeerConnection(peerConnectionModel.getPeerConnectionId());
            }
        }

        result.addIceLocalCandidate()
                .addIceRemoteCandidate()
                .addOperationSystem()
                .addIceServer("https://IceServer.com")
                .addMediaCodec()
                .addMediaDevice()
                .addMediaSource()
                .addUserMediaError("userMediaError")
                .addLocalSdp("a=candidate:2 1 UDP 1694498815 192.0.2.3 45664 typ srflx raddr 10.0.1.1 rport 8998")
//                .addDataChannel(peerConnectionId, UUID.randomUUID().toString())
//                .addInboundAudioTrack(peerConnectionId, UUID.randomUUID().toString(), clientInbAudioSSRC, audioStreamId, audioSinkId)
//                .addInboundVideoTrack(peerConnectionId, UUID.randomUUID().toString(), clientInbVideoSSRC, videoStreamId, videoSinkId)
//                .addOutboundAudioTrack(peerConnectionId, UUID.randomUUID().toString(), clientOutbAudioSSRC, audioStreamId)
//                .addOutboundVideoTrack(peerConnectionId, UUID.randomUUID().toString(), clientOutbVideoSSRC, videoStreamId)
        ;

        for (var inboundTrack : this.modelsMapGenerator.getInboundTrackModels().values()) {
            if (!inboundTrack.getClientId().equals(clientId)) {
                continue;
            }
            var peerConnectionId = inboundTrack.getPeerConnectionId();
            var trackId = inboundTrack.getTrackId();
            var ssrc = inboundTrack.getSsrc(0);
            var streamId = inboundTrack.hasSfuStreamId() ? inboundTrack.getSfuStreamId() : null;
            var sinkId = inboundTrack.hasSfuSinkId() ? inboundTrack.getSfuSinkId() : null;
            var kind = MediaKind.valueOf(inboundTrack.getKind());
            if (kind == MediaKind.AUDIO) {
                result.addInboundAudioTrack(peerConnectionId, trackId, ssrc, streamId, sinkId);
            } else if (kind == MediaKind.VIDEO){
                result.addInboundVideoTrack(peerConnectionId, trackId, ssrc, streamId, sinkId);
            }
        }
        for (var outboundTrack : this.modelsMapGenerator.getOutboundTrackModels().values()) {
            if (!outboundTrack.getClientId().equals(clientId)) {
                continue;
            }
            var peerConnectionId = outboundTrack.getPeerConnectionId();
            var trackId = outboundTrack.getTrackId();
            var ssrc = outboundTrack.getSsrc(0);
            var streamId = outboundTrack.hasSfuStreamId() ? outboundTrack.getSfuStreamId() : null;
            var kind = MediaKind.valueOf(outboundTrack.getKind());
            if (kind == MediaKind.AUDIO) {
                result.addOutboundAudioTrack(peerConnectionId, trackId, ssrc, streamId);
            } else if (kind == MediaKind.VIDEO){
                result.addOutboundVideoTrack(peerConnectionId, trackId, ssrc, streamId);
            }
        }
        return result;
    }
}
