package org.observertc.webrtc.observer.evaluatorsPurgatory.pcSampleToReportsV3;

import java.util.*;

public class ObservedPcState {
    public final List<ObservedPCS> samples = new LinkedList<>();
    public final Map<UUID, UUID> newPcToUUIDs = new HashMap<>();
    public final Set<UUID> expiredPcUUIDs = new HashSet<>();
}
