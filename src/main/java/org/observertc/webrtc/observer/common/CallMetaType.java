package org.observertc.webrtc.observer.common;

import org.apache.avro.Schema;

public enum CallMetaType {
    CERTIFICATE,
    CODEC,
    ICE_LOCAL_CANDIDATE,
    ICE_REMOTE_CANDIDATE,
    ICE_SERVER,
    MEDIA_CONSTRAINT,
    MEDIA_DEVICE,
    MEDIA_SOURCE,
    USER_MEDIA_ERROR,
}
