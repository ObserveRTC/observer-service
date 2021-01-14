package org.observertc.webrtc.observer.evaluators;

import java.time.Instant;
import java.util.UUID;

class ICECandidatePairUpdate {
    public static ICECandidatePairUpdate of(
            String localCandidateId,
            String remoteCandidateId,
            UUID serviceUUID,
            UUID pcUUID,
            String mediaUnitId
    ) {
        ICECandidatePairUpdate result = new ICECandidatePairUpdate();
        result.localCandidateId = localCandidateId;
        result.remoteCandidateId = remoteCandidateId;
        result.pcUUID = pcUUID;
        result.serviceUUID = serviceUUID;
        result.mediaUnitId = mediaUnitId;
        return result;
    }
    public String localCandidateId;
    public String remoteCandidateId;
    public UUID serviceUUID;
    public UUID pcUUID;
    public String mediaUnitId;
    public final Instant created = Instant.now();
    public Instant updated = Instant.now();
    public boolean processed = false;
}
