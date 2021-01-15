package org.observertc.webrtc.observer.monitorstats;

import java.util.HashMap;
import java.util.Map;

public class CallStats {
    public int p2pCalls = 0;
    public int p2pStreams = 0;
    public Map<String, MediaUnitStats> mediaUnitStats = new HashMap<>();
    public int unknownTypeOfCalls = 0;
    public Integer unknownTypeOfStreams = 0;

    public static class MediaUnitStats {
        public int totalCalls = 0;
        public int ownedCalls = 0;
        public int sharedCalls = 0;
        public int concurrentStreams = 0;
    }
}
