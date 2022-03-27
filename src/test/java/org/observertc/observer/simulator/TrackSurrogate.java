package org.observertc.observer.simulator;

import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.utils.RandomGenerators;
import org.observertc.observer.utils.TestUtils;

import java.util.UUID;

public class TrackSurrogate {
    private static final RandomGenerators generator = new RandomGenerators();

    public static TrackSurrogate createFromSession(UUID peerConnectionId, RtpSessionSurrogate session) {
        var result = new TrackSurrogate(session.SSRC);
        result.kind = TestUtils.AUDIO_KIND;
        result.peerConnectionId = peerConnectionId;
        result.sfuStreamId = session.sfuStreamId;
        result.sfuSinkId = session.sfuSinkId;
        return result;
    }

    public UUID peerConnectionId;
    public final UUID trackId = UUID.randomUUID();
    public final Long SSRC;
    public UUID sfuStreamId;
    public UUID sfuSinkId;
    public String kind;
    public StreamDirection direction;

    private TrackSurrogate(Long ssrc) {
        SSRC = ssrc;
    }


}
