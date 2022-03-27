package org.observertc.observer.simulator;

import org.observertc.observer.utils.RandomGenerators;
import org.observertc.observer.utils.TestUtils;

import java.util.UUID;

public class RtpSessionSurrogate {
    private static final RandomGenerators generator = new RandomGenerators();

    public static RtpSessionSurrogate create(String mediaKind) {
        switch (mediaKind) {
            case TestUtils.AUDIO_KIND:
                return createAudioSession();
            case TestUtils.VIDEO_KIND:
                return createVideoSession();
            default:
                throw new RuntimeException("Unsupported media kind: " + mediaKind);
        }
    }

    public static RtpSessionSurrogate createAudioSession() {
        Long SSRC = generator.getRandomSSRC();
        var result = new RtpSessionSurrogate(SSRC);
        result.kind = TestUtils.AUDIO_KIND;
        return result;
    }

    public static RtpSessionSurrogate createVideoSession() {
        Long SSRC = generator.getRandomSSRC();
        var result = new RtpSessionSurrogate(SSRC);
        result.kind = TestUtils.VIDEO_KIND;
        return result;
    }

    public final Long SSRC;
    public UUID sfuStreamId;
    public UUID sfuSinkId;
    public String kind;

    private RtpSessionSurrogate(Long ssrc) {
        SSRC = ssrc;
    }
}
