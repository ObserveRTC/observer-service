package org.observertc.webrtc.observer.samples;

import java.util.*;
import java.util.stream.Stream;

/**
 * Call assigned and organized ObservedSamples
 */
public class CollectedCallSamples implements Iterable<CallSamples>{

    private final Map<UUID, CallSamples> samples = new HashMap<>();
    private final Set<MediaTrackId> inboundMediaTrackIds = new HashSet<>();

    public static Builder builder() {
        return new Builder();
    }

    private CollectedCallSamples() {

    }

    @Override
    public Iterator<CallSamples> iterator() {
        return this.samples.values().iterator();
    }

    public Stream<CallSamples> stream() {
        return this.samples.values().stream();
    }

    public Set<UUID> getCallIds() {
        return this.samples.keySet();
    }

    public Set<MediaTrackId> getInboundMediaTrackIds() {
        return this.inboundMediaTrackIds;
    }

    public static class Builder {
        private final CollectedCallSamples result = new CollectedCallSamples();

        public CollectedCallSamples.Builder withCallSamples(CallSamples callSamples) {
            this.result.samples.put(callSamples.getCallId(), callSamples);
            for (ClientSamples clientSamples : callSamples) {
                Set<MediaTrackId> inboundMediaTrackIds = clientSamples.getInboundMediaTrackIds();
                this.result.inboundMediaTrackIds.addAll(inboundMediaTrackIds);
            }
            return this;
        }

        public CollectedCallSamples build() {
            return this.result;
        }
    }
}
