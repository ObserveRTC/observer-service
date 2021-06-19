package org.observertc.webrtc.observer.samples;

import java.util.Objects;
import java.util.UUID;

public class MediaTrackId {
    private static final String DELIMITER = "##/##";
    public static String getKey(MediaTrackId mediaTrackId) {
        return String.join(DELIMITER, mediaTrackId.peerConnectionId.toString(), mediaTrackId.ssrc.toString());
    }

    public static MediaTrackId fromKey(String serviceRoomIdKey) {
        String[] parts = serviceRoomIdKey.split(DELIMITER);
        UUID peerConnectionId = UUID.fromString(parts[0]);
        Long ssrc = Long.parseLong(parts[1]);
        return new MediaTrackId(peerConnectionId, ssrc);
    }
    public static MediaTrackId make(UUID peerConnectionId, Long ssrc) {
        return new MediaTrackId(peerConnectionId, ssrc);
    }

    public final UUID peerConnectionId;
    public final Long ssrc;

    public MediaTrackId(UUID peerConnectionId, Long ssrc) {
        this.peerConnectionId = peerConnectionId;
        this.ssrc = ssrc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.peerConnectionId, this.ssrc);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        MediaTrackId otherServiceRoomId = (MediaTrackId) other;
        if (!otherServiceRoomId.ssrc.equals(this.ssrc)) return false;
        if (!otherServiceRoomId.peerConnectionId.equals(this.peerConnectionId)) return false;
        return true;
    }
}
