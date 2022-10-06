package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.utils.ModelsMapGenerator;
import org.observertc.schemas.dtos.Models;

import java.util.Map;

@MicronautTest
class MatchTracksTest {

    private final ModelsMapGenerator modelsMapGenerator = new ModelsMapGenerator();

    @Inject
    BeanProvider<MatchTracks> matchTracksBeanProvider;

    @Inject
    HamokStorages hamokStorages;

    @Test
    public void matchP2p() {
        this.modelsMapGenerator.generateP2pCase().saveTo(this.hamokStorages);
        var inboundTrackModels = this.modelsMapGenerator.getInboundTrackModels();
        var outboundTrackModels = this.modelsMapGenerator.getOutboundTrackModels();

        var matches = this.matchTracksBeanProvider.get()
                .whereOutboundTrackIds(outboundTrackModels.keySet())
                .whereInboundTrackIds(inboundTrackModels.keySet())
                .execute()
                .getResult();

        this.assertMatches(outboundTrackModels, inboundTrackModels, matches);
    }

    @Test
    public void matchSingleSfu() {
        this.modelsMapGenerator.generateSingleSfuCase().saveTo(this.hamokStorages);
        var inboundTrackModels = this.modelsMapGenerator.getInboundTrackModels();
        var outboundTrackModels = this.modelsMapGenerator.getOutboundTrackModels();

        var matches = this.matchTracksBeanProvider.get()
                .whereOutboundTrackIds(outboundTrackModels.keySet())
                .whereInboundTrackIds(inboundTrackModels.keySet())
                .execute()
                .getResult();

        this.assertMatches(outboundTrackModels, inboundTrackModels, matches);
    }


    private void assertMatches(Map<String, Models.OutboundTrack> outboundTrackModels,
                               Map<String, Models.InboundTrack> inboundTrackModels,
                               MatchTracks.Report matches) {
        var inboundMatches = matches.inboundMatches;
        Assertions.assertEquals(inboundTrackModels.size(), inboundMatches.size());
        for (var inboundTrackModel : inboundTrackModels.values()) {
            var match = inboundMatches.get(inboundTrackModel.getTrackId());
            var outboundTrackModel = outboundTrackModels.get(match.outboundTrackId());

            Assertions.assertEquals(inboundTrackModel.getTrackId(), match.inboundTrackId());
            Assertions.assertEquals(inboundTrackModel.getPeerConnectionId(), match.inboundPeerConnectionId());
            Assertions.assertEquals(inboundTrackModel.getUserId(), match.inboundUserId());
            Assertions.assertEquals(inboundTrackModel.getClientId(), match.inboundClientId());

            Assertions.assertEquals(inboundTrackModel.getCallId(), match.callId());

            Assertions.assertEquals(outboundTrackModel.getClientId(), match.outboundClientId());
            Assertions.assertEquals(outboundTrackModel.getPeerConnectionId(), match.outboundPeerConnectionId());
            Assertions.assertEquals(outboundTrackModel.getUserId(), match.outboundUserId());
            Assertions.assertEquals(outboundTrackModel.getTrackId(), match.outboundTrackId());
        }
    }
}