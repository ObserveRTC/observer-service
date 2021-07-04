package org.observertc.webrtc.observer.samples;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Call assigned and organized ObservedSamples
 */
public class CollectedCallSamples implements Iterable<CallSamples>{

    private final Map<UUID, CallSamples> samples = new HashMap<>();
    private final Set<UUID> inboundMediaTrackIds = new HashSet<>();

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

    public Set<UUID> getInboundMediaTrackIds() {
        return this.inboundMediaTrackIds;
    }

    public Set<UUID> getClientIds() {
        return this.samples.values().stream().flatMap(clientSamples -> clientSamples.getClientIds().stream()).collect(Collectors.toSet());
    }

    public Set<UUID> getPeerConnectionIds() {
        return this.samples.values()
                .stream()
                .flatMap(clientSamples -> clientSamples.getPeerConnectionIds().stream())
                .collect(Collectors.toSet());
    }

    public Set<UUID> getMediaTrackIds() {
        return this.samples.values()
                .stream()
                .map(clientSamples -> Arrays.asList(
                        clientSamples.getInboundAudioTrackIds(),
                        clientSamples.getInboundVideoTrackIds(),
                        clientSamples.getOutboundAudioTrackIds(),
                        clientSamples.getOutboundVideoTrackIds()
                ))
                .flatMap(array -> array.stream())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public static class Builder {
        private final CollectedCallSamples result = new CollectedCallSamples();

        public CollectedCallSamples.Builder withCallSamples(CallSamples callSamples) {
            this.result.samples.put(callSamples.getCallId(), callSamples);
            for (ClientSamples clientSamples : callSamples) {
                Set<UUID> inboundMediaTrackIds = clientSamples.getInboundMediaTrackIds();
                this.result.inboundMediaTrackIds.addAll(inboundMediaTrackIds);
            }
            return this;
        }

        public CollectedCallSamples build() {
            return this.result;
        }
    }
}
