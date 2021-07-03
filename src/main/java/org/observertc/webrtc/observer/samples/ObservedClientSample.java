package org.observertc.webrtc.observer.samples;

import java.util.UUID;

public interface ObservedClientSample extends ObservedSample {

    UUID getClientId();

    ClientSample getClientSample();

    String getMarker();

    String getUserId();

    int getSampleSeq();
}
