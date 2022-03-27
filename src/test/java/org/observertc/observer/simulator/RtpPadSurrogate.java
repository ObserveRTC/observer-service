package org.observertc.observer.simulator;

import java.util.UUID;

public class RtpPadSurrogate {
    public static RtpPadSurrogate createRtpPad(UUID transportId, RtpSessionSurrogate rtpSession) {
        var result = new RtpPadSurrogate(transportId, rtpSession.SSRC, rtpSession.kind);
        result.streamId = rtpSession.sfuStreamId;
        result.sinkId = rtpSession.sfuSinkId;
        return result;
    }

    public final UUID transportId;
    public final UUID padId = UUID.randomUUID();
    public final Long SSRC;
    public final String kind;
    public UUID streamId;
    public UUID sinkId;

    private RtpPadSurrogate(UUID transportId, Long ssrc, String kind) {
        this.transportId = transportId;
        SSRC = ssrc;
        this.kind = kind;
    }


}
